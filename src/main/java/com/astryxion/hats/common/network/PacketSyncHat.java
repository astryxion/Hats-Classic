package com.astryxion.hats.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncHat {

    private final CompoundTag data;

    public PacketSyncHat(CompoundTag tag) {
        this.data = tag;
    }

    public CompoundTag data() {
        return data;
    }

    public static void encode(PacketSyncHat msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static PacketSyncHat decode(FriendlyByteBuf buf) {
        return new PacketSyncHat(buf.readNbt());
    }

    public static void handle(PacketSyncHat msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.network.HatClientPacketHandlers",
                "handleSyncHat",
                new Class<?>[]{PacketSyncHat.class},
                new Object[]{msg}));
        ctx.get().setPacketHandled(true);
    }
}