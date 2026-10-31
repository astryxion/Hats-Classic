package com.astryxion.hats.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.Supplier;

public class PacketSyncHat implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketSyncHat> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "sync_hat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncHat> STREAM_CODEC =
            StreamCodec.of(PacketSyncHat::encode, PacketSyncHat::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final CompoundTag data;

    public PacketSyncHat(CompoundTag tag) {
        this.data = tag;
    }

    public CompoundTag data() {
        return data;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketSyncHat msg) {
        buf.writeNbt(msg.data);
    }

    public static PacketSyncHat decode(RegistryFriendlyByteBuf buf) {
        return new PacketSyncHat(buf.readNbt());
    }

    public static void handle(PacketSyncHat msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.network.HatClientPacketHandlers",
                "handleSyncHat",
                new Class<?>[]{PacketSyncHat.class},
                new Object[]{msg}));
        
    }
}