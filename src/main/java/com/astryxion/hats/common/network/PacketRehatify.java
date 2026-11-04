package com.astryxion.hats.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketRehatify implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketRehatify> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "rehatify"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRehatify> STREAM_CODEC =
            StreamCodec.of(PacketRehatify::encode, PacketRehatify::decode);

    private final int entityId;

    public PacketRehatify(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketRehatify msg) {
        buf.writeInt(msg.entityId);
    }

    public static PacketRehatify decode(RegistryFriendlyByteBuf buf) {
        return new PacketRehatify(buf.readInt());
    }

    public static void handle(PacketRehatify msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientPacketCalls.handlers(
                "handleRehatify",
                new Class<?>[] {int.class},
                msg.entityId));
    }
}
