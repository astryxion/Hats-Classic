package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.trade.TradeOffer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketIncomingTradeOffer {

    private final TradeOffer offer;

    public PacketIncomingTradeOffer(TradeOffer offer) {
        this.offer = offer;
    }

    public TradeOffer offer() {
        return offer;
    }

    public static void encode(PacketIncomingTradeOffer msg, FriendlyByteBuf buf) {
        TradeOffer offer = msg.offer;
        buf.writeUUID(offer.offerId);
        buf.writeUUID(offer.senderUuid);
        buf.writeUtf(offer.senderName);
        buf.writeUUID(offer.receiverUuid);
        buf.writeUtf(offer.receiverName);
        TradePacketBuffers.writeIds(buf, offer.senderHats);
        TradePacketBuffers.writeIds(buf, offer.receiverHats);
        buf.writeLong(offer.createdAtMs);
    }

    public static PacketIncomingTradeOffer decode(FriendlyByteBuf buf) {
        UUID offerId = buf.readUUID();
        UUID senderUuid = buf.readUUID();
        String senderName = buf.readUtf();
        UUID receiverUuid = buf.readUUID();
        String receiverName = buf.readUtf();
        List<net.minecraft.resources.ResourceLocation> senderHats = TradePacketBuffers.readIds(buf);
        List<net.minecraft.resources.ResourceLocation> receiverHats = TradePacketBuffers.readIds(buf);
        long createdAt = buf.readLong();
        return new PacketIncomingTradeOffer(new TradeOffer(
                offerId, senderUuid, senderName, receiverUuid, receiverName, senderHats, receiverHats, createdAt));
    }

    public static void handle(PacketIncomingTradeOffer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.astryxion.hats.client.trade.ClientTradePackets.onIncomingOffer(msg)));
        ctx.get().setPacketHandled(true);
    }
}
