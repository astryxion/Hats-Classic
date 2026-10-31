package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketTradeOfferRevoked {

    private final UUID offerId;

    public PacketTradeOfferRevoked(UUID offerId) {
        this.offerId = offerId;
    }

    public UUID offerId() {
        return offerId;
    }

    public static void encode(PacketTradeOfferRevoked msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.offerId);
    }

    public static PacketTradeOfferRevoked decode(FriendlyByteBuf buf) {
        return new PacketTradeOfferRevoked(buf.readUUID());
    }

    public static void handle(PacketTradeOfferRevoked msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.trade.ClientTradePackets",
                "onRevoked",
                new Class<?>[]{PacketTradeOfferRevoked.class},
                new Object[]{msg}));
        ctx.get().setPacketHandled(true);
    }
}
