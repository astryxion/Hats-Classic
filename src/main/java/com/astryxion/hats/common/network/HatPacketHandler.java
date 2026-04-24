package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Consumer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class HatPacketHandler {

    /** Set from client entry only; avoids loading client classes on dedicated server. */
    private static Consumer<PacketHatUnlocked> hatUnlockedClientHandler = msg -> {};

    public static void bindHatUnlockedClientHandler(Consumer<PacketHatUnlocked> handler) {
        hatUnlockedClientHandler = handler;
    }

    public static final ResourceLocation CHANNEL_ID =
            ResourceLocation.fromNamespaceAndPath(Hats.MODID, "main");
    private static final ResourceLocation CHANNEL_ID_C2S = ResourceLocation.fromNamespaceAndPath(Hats.MODID, "main_c2s");
    private static final ResourceLocation CHANNEL_ID_S2C = ResourceLocation.fromNamespaceAndPath(Hats.MODID, "main_s2c");

    private static final byte ID_EQUIP = 0;
    private static final byte ID_OPEN_COSMETIC = 1;
    private static final byte ID_OPEN_HUNTING = 2;
    private static final byte ID_SYNC_HAT = 3;
    private static final byte ID_HAT_UNLOCKED = 4;
    private static final byte ID_SYNC_HAT_PART = 5;

    public record HatsC2SPayload(byte typeId, byte[] payload) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<HatsC2SPayload> TYPE = new CustomPacketPayload.Type<>(CHANNEL_ID_C2S);
        public static final StreamCodec<FriendlyByteBuf, HatsC2SPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BYTE, HatsC2SPayload::typeId,
                ByteBufCodecs.byteArray(32767), HatsC2SPayload::payload,
                HatsC2SPayload::new);
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record HatsS2CPayload(byte typeId, byte[] payload) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<HatsS2CPayload> TYPE = new CustomPacketPayload.Type<>(CHANNEL_ID_S2C);
        public static final StreamCodec<FriendlyByteBuf, HatsS2CPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BYTE, HatsS2CPayload::typeId,
                ByteBufCodecs.byteArray(32767), HatsS2CPayload::payload,
                HatsS2CPayload::new);
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, HatPacketHandler::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Hats.MODID).versioned("1");

        registrar.playToServer(HatsC2SPayload.TYPE, HatsC2SPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
                FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.payload()));
                try {
                    switch (payload.typeId()) {
                        case ID_EQUIP -> {
                            PacketEquipHat msg = PacketEquipHat.decode(copy);
                            PacketEquipHat.handle(msg, serverPlayer);
                        }
                        case ID_OPEN_COSMETIC -> {
                            PacketOpenCosmeticMenu msg = new PacketOpenCosmeticMenu();
                            PacketOpenCosmeticMenu.handle(msg, serverPlayer);
                        }
                        case ID_OPEN_HUNTING -> {
                            PacketOpenHuntingMenu msg = new PacketOpenHuntingMenu();
                            PacketOpenHuntingMenu.handle(msg, serverPlayer);
                        }
                        default -> {}
                    }
                } finally {
                    copy.release();
                }
            });
        });

        registrar.playToClient(HatsS2CPayload.TYPE, HatsS2CPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.payload()));
                try {
                    switch (payload.typeId()) {
                        case ID_SYNC_HAT -> {
                            PacketSyncHat msg = PacketSyncHat.decode(copy);
                            PacketSyncHat.handle(msg);
                        }
                        case ID_HAT_UNLOCKED -> {
                            var level = context.player().level();
                            if (level == null) return;
                            var registryAccess = level.registryAccess();
                            PacketHatUnlocked msg = PacketHatUnlocked.decode(copy, registryAccess);
                            hatUnlockedClientHandler.accept(msg);
                        }
                        case ID_SYNC_HAT_PART -> {
                            PacketSyncHatPart msg = PacketSyncHatPart.decode(copy);
                            PacketSyncHatPart.handle(msg);
                        }
                        default -> {}
                    }
                } finally {
                    copy.release();
                }
            });
        });
    }

    public static void sendToPlayer(ServerPlayer player, byte packetId, FriendlyByteBuf buf) {
        byte[] arr = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), arr);
        sendToPlayer(player, packetId, arr);
    }

    public static void sendToPlayer(ServerPlayer player, byte packetId, byte[] payload) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new HatsS2CPayload(packetId, payload));
    }

    public static void sendEquipToServer(String hatId) {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_EQUIP);
        PacketEquipHat.encode(new PacketEquipHat(hatId), out);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new HatsC2SPayload(ID_EQUIP, arr));
    }

    public static void sendOpenCosmeticToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_OPEN_COSMETIC);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new HatsC2SPayload(ID_OPEN_COSMETIC, arr));
    }

    public static void sendOpenHuntingToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_OPEN_HUNTING);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new HatsC2SPayload(ID_OPEN_HUNTING, arr));
    }

    public static void sendSyncHatToPlayer(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketSyncHat.encode(new PacketSyncHat(data), buf);
        sendToPlayer(player, ID_SYNC_HAT, buf);
    }

    public static void sendHatUnlockedToPlayer(ServerPlayer player, net.minecraft.world.item.ItemStack hatStack) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        PacketHatUnlocked.encode(new PacketHatUnlocked(hatStack), buf, player.registryAccess());
        byte[] arr = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), arr);
        buf.release();
        sendToPlayer(player, ID_HAT_UNLOCKED, arr);
    }

    public static void sendSyncHatPartToPlayer(ServerPlayer player, int entityId, net.minecraft.nbt.CompoundTag tag) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketSyncHatPart.encode(new PacketSyncHatPart(entityId, tag), buf);
        sendToPlayer(player, ID_SYNC_HAT_PART, buf);
    }
}
