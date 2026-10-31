package com.astryxion.hats.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRehatify {

    private final int entityId;

    public PacketRehatify(int entityId) {
        this.entityId = entityId;
    }

    public int entityId() {
        return entityId;
    }

    public static void encode(PacketRehatify msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static PacketRehatify decode(FriendlyByteBuf buf) {
        return new PacketRehatify(buf.readInt());
    }

    public static void handle(PacketRehatify msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.network.HatClientPacketHandlers",
                "handleRehatify",
                new Class<?>[]{PacketRehatify.class},
                new Object[]{msg}));
        ctx.get().setPacketHandled(true);
    }
}
