package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;
import com.mojang.serialization.DataResult;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class HatPacketHandler {

    public static final Identifier CHANNEL_ID =
            Identifier.fromNamespaceAndPath(Hats.MODID, "main");
    private static final Identifier CHANNEL_ID_C2S = Identifier.fromNamespaceAndPath(Hats.MODID, "main_c2s");
    private static final Identifier CHANNEL_ID_S2C = Identifier.fromNamespaceAndPath(Hats.MODID, "main_s2c");

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

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(HatsC2SPayload.TYPE, HatsC2SPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HatsS2CPayload.TYPE, HatsS2CPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(HatsC2SPayload.TYPE, (payload, context) -> {
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
    }

    public static void sendToPlayer(ServerPlayer player, byte packetId, FriendlyByteBuf buf) {
        byte[] arr = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), arr);
        sendToPlayer(player, packetId, arr);
    }

    public static void sendToPlayer(ServerPlayer player, byte packetId, byte[] payload) {
        ServerPlayNetworking.send(player, new HatsS2CPayload(packetId, payload));
    }

    public static void sendSyncHatToPlayer(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketSyncHat.encode(new PacketSyncHat(data), buf);
        sendToPlayer(player, ID_SYNC_HAT, buf);
    }

    public static void sendHatUnlockedToPlayer(ServerPlayer player, ItemStack hatStack) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        var ops = RegistryOps.create(NbtOps.INSTANCE, player.registryAccess());
        DataResult<Tag> encoded = ItemStack.CODEC.encodeStart(ops, hatStack);
        buf.writeNbt((CompoundTag) encoded.getOrThrow());
        byte[] arr = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), arr);
        buf.release();
        sendToPlayer(player, ID_HAT_UNLOCKED, arr);
    }

    public static void sendSyncHatPartToPlayer(ServerPlayer player, int entityId, net.minecraft.nbt.CompoundTag tag) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(entityId);
        buf.writeNbt(tag);
        sendToPlayer(player, ID_SYNC_HAT_PART, buf);
    }

    /**
     * Syncs one player's equipped hat part to every client that is tracking that player (including their own).
     * Required for multiplayer: {@link #sendSyncHatPartToPlayer} alone only updates a single recipient.
     */
    public static void sendSyncHatPartToTracking(ServerPlayer trackedPlayer, int entityId, net.minecraft.nbt.CompoundTag tag) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeInt(entityId);
            buf.writeNbt(tag);
            byte[] arr = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), arr);
            HatsS2CPayload payload = new HatsS2CPayload(ID_SYNC_HAT_PART, arr);
            boolean selfSent = false;
            for (ServerPlayer viewer : PlayerLookup.tracking(trackedPlayer)) {
                ServerPlayNetworking.send(viewer, payload);
                if (viewer == trackedPlayer) {
                    selfSent = true;
                }
            }
            if (!selfSent) {
                ServerPlayNetworking.send(trackedPlayer, payload);
            }
        } finally {
            buf.release();
        }
    }
}
