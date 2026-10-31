package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketTradePartnerLeft {

    private final UUID targetUuid;

    public PacketTradePartnerLeft(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    public static void encode(PacketTradePartnerLeft msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.targetUuid);
    }

    public static PacketTradePartnerLeft decode(FriendlyByteBuf buf) {
        return new PacketTradePartnerLeft(buf.readUUID());
    }

    public static void handle(PacketTradePartnerLeft msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.trade.ClientTradePackets",
                "onPartnerLeft",
                new Class<?>[]{PacketTradePartnerLeft.class},
                new Object[]{msg}));
        ctx.get().setPacketHandled(true);
    }
}
