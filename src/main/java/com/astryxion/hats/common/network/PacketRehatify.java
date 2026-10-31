package com.astryxion.hats.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.Supplier;

public class PacketRehatify implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketRehatify> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "rehatify"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRehatify> STREAM_CODEC =
            StreamCodec.of(PacketRehatify::encode, PacketRehatify::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final int entityId;

    public PacketRehatify(int entityId) {
        this.entityId = entityId;
    }

    public int entityId() {
        return entityId;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketRehatify msg) {
        buf.writeInt(msg.entityId);
    }

    public static PacketRehatify decode(RegistryFriendlyByteBuf buf) {
        return new PacketRehatify(buf.readInt());
    }

    public static void handle(PacketRehatify msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.network.HatClientPacketHandlers",
                "handleRehatify",
                new Class<?>[]{PacketRehatify.class},
                new Object[]{msg}));
        
    }
}
