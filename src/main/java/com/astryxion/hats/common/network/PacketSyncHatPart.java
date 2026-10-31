package com.astryxion.hats.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncHatPart {

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

    public static void encode(PacketSyncHatPart msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.playerId);
        buf.writeNbt(msg.tag);
    }

    public static PacketSyncHatPart decode(FriendlyByteBuf buf) {
        return new PacketSyncHatPart(buf.readInt(), buf.readNbt());
    }

    public static void handle(PacketSyncHatPart msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.network.HatClientPacketHandlers",
                "handleSyncHatPart",
                new Class<?>[]{PacketSyncHatPart.class},
                new Object[]{msg}));
        ctx.get().setPacketHandled(true);
    }
}