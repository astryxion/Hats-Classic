package com.astryxion.hats.common.network;

import com.astryxion.hats.common.capability.HatDataCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class PacketSyncHat {

    private final CompoundTag data;

    public PacketSyncHat(CompoundTag tag) {
        this.data = tag;
    }

    public static void encode(PacketSyncHat msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static PacketSyncHat decode(FriendlyByteBuf buf) {
        return new PacketSyncHat(buf.readNbt());
    }

    public static void handle(PacketSyncHat msg) {
        if (msg.data != null) {
            HatDataCapability.setClientData(msg.data);
        }
    }
}
