package com.astryxion.hats.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSyncHatPart implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncHatPart> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "sync_hat_part"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncHatPart> STREAM_CODEC =
            StreamCodec.of(PacketSyncHatPart::encode, PacketSyncHatPart::decode);

    private final int playerId;
    private final CompoundTag tag;

    public PacketSyncHatPart(int playerId, CompoundTag tag) {
        this.playerId = playerId;
        this.tag = tag;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketSyncHatPart msg) {
        buf.writeInt(msg.playerId);
        buf.writeNbt(msg.tag);
    }

    public static PacketSyncHatPart decode(RegistryFriendlyByteBuf buf) {
        return new PacketSyncHatPart(buf.readInt(), buf.readNbt());
    }

    public static void handle(PacketSyncHatPart msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientPacketCalls.handlers(
                "handleSyncHatPart",
                new Class<?>[] {int.class, CompoundTag.class},
                msg.playerId,
                msg.tag));
    }

    /** Spawn packets can arrive before the client has the entity. */
    public static void flushPending() {
        ClientPacketCalls.handlers("flushPendingHatParts", new Class<?>[0]);
    }
}
