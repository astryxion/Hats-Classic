package com.astryxion.hats.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.Supplier;

public class PacketSyncHatPart implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketSyncHatPart> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "sync_hat_part"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncHatPart> STREAM_CODEC =
            StreamCodec.of(PacketSyncHatPart::encode, PacketSyncHatPart::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final int playerId;
    private final CompoundTag tag;

    public PacketSyncHatPart(int playerId, CompoundTag tag) {
        this.playerId = playerId;
        this.tag = tag;
    }

    public int playerId() {
        return playerId;
    }

    public CompoundTag tag() {
        return tag;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketSyncHatPart msg) {
        buf.writeInt(msg.playerId);
        buf.writeNbt(msg.tag);
    }

    public static PacketSyncHatPart decode(RegistryFriendlyByteBuf buf) {
        return new PacketSyncHatPart(buf.readInt(), buf.readNbt());
    }

    public static void handle(PacketSyncHatPart msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.network.HatClientPacketHandlers",
                "handleSyncHatPart",
                new Class<?>[]{PacketSyncHatPart.class},
                new Object[]{msg}));
        
    }
}