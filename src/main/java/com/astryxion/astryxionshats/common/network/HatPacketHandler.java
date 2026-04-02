package com.astryxion.astryxionshats.common.network;

import com.astryxion.astryxionshats.AstryxionsHats;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class HatPacketHandler {

    public static final ResourceLocation CHANNEL_ID =
            ResourceLocation.fromNamespaceAndPath(AstryxionsHats.MODID, "main");
    private static final ResourceLocation CHANNEL_ID_C2S = ResourceLocation.fromNamespaceAndPath(AstryxionsHats.MODID, "main_c2s");
    private static final ResourceLocation CHANNEL_ID_S2C = ResourceLocation.fromNamespaceAndPath(AstryxionsHats.MODID, "main_s2c");

    private static final byte ID_EQUIP = 0;
    private static final byte ID_OPEN_COSMETIC = 1;
    private static final byte ID_OPEN_HUNTING = 2;
    private static final byte ID_SYNC_HAT = 3;
    private static final byte ID_HAT_UNLOCKED = 4;
    private static final byte ID_SYNC_HAT_PART = 5;

    public record HatsC2SPayload(byte typeId, byte[] payload) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<HatsC2SPayload> TYPE = new CustomPacketPayload.Type<>(CHANNEL_ID_C2S);
        public static final StreamCodec<RegistryFriendlyByteBuf, HatsC2SPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BYTE, HatsC2SPayload::typeId,
                ByteBufCodecs.byteArray(32767), HatsC2SPayload::payload,
                HatsC2SPayload::new);
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
        public void write(FriendlyByteBuf buf) { buf.writeByte(typeId); buf.writeByteArray(payload); }
    }

    public record HatsS2CPayload(byte typeId, byte[] payload) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<HatsS2CPayload> TYPE = new CustomPacketPayload.Type<>(CHANNEL_ID_S2C);
        public static final StreamCodec<RegistryFriendlyByteBuf, HatsS2CPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BYTE, HatsS2CPayload::typeId,
                ByteBufCodecs.byteArray(32767), HatsS2CPayload::payload,
                HatsS2CPayload::new);
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
        public void write(FriendlyByteBuf buf) { buf.writeByte(typeId); buf.writeByteArray(payload); }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(HatsC2SPayload.TYPE, HatsC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HatsS2CPayload.TYPE, HatsS2CPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(HatsC2SPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.payload()));
                try {
                    switch (payload.typeId()) {
                        case ID_EQUIP -> {
                            PacketEquipHat msg = PacketEquipHat.decode(copy);
                            PacketEquipHat.handle(msg, context.player());
                        }
                        case ID_OPEN_COSMETIC -> {
                            PacketOpenCosmeticMenu msg = new PacketOpenCosmeticMenu();
                            PacketOpenCosmeticMenu.handle(msg, context.player());
                        }
                        case ID_OPEN_HUNTING -> {
                            PacketOpenHuntingMenu msg = new PacketOpenHuntingMenu();
                            PacketOpenHuntingMenu.handle(msg, context.player());
                        }
                        default -> {}
                    }
                } finally {
                    copy.release();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(HatsS2CPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.payload()));
                try {
                    switch (payload.typeId()) {
                        case ID_SYNC_HAT -> {
                            PacketSyncHat msg = PacketSyncHat.decode(copy);
                            PacketSyncHat.handle(msg);
                        }
                        case ID_HAT_UNLOCKED -> {
                            PacketHatUnlocked msg = PacketHatUnlocked.decode(copy, context.client().getConnection().registryAccess());
                            PacketHatUnlocked.handle(msg);
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
        if (ServerPlayNetworking.canSend(player, HatsS2CPayload.TYPE)) {
            byte[] arr = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), arr);
            ServerPlayNetworking.send(player, new HatsS2CPayload(packetId, arr));
        }
    }

    public static void sendEquipToServer(String hatId) {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_EQUIP);
        PacketEquipHat.encode(new PacketEquipHat(hatId), out);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        ClientPlayNetworking.send(new HatsC2SPayload(ID_EQUIP, arr));
    }

    public static void sendOpenCosmeticToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_OPEN_COSMETIC);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        ClientPlayNetworking.send(new HatsC2SPayload(ID_OPEN_COSMETIC, arr));
    }

    public static void sendOpenHuntingToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_OPEN_HUNTING);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        ClientPlayNetworking.send(new HatsC2SPayload(ID_OPEN_HUNTING, arr));
    }

    public static void sendSyncHatToPlayer(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketSyncHat.encode(new PacketSyncHat(data), buf);
        sendToPlayer(player, ID_SYNC_HAT, buf);
    }

    public static void sendHatUnlockedToPlayer(ServerPlayer player, net.minecraft.world.item.ItemStack hatStack) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketHatUnlocked.encode(new PacketHatUnlocked(hatStack), buf, player.getServer().registryAccess());
        sendToPlayer(player, ID_HAT_UNLOCKED, buf);
    }

    public static void sendSyncHatPartToPlayer(ServerPlayer player, int entityId, net.minecraft.nbt.CompoundTag tag) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketSyncHatPart.encode(new PacketSyncHatPart(entityId, tag), buf);
        sendToPlayer(player, ID_SYNC_HAT_PART, buf);
    }
}
