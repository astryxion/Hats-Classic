package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.trade.TradeOffer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public class PacketIncomingTradeOffer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketIncomingTradeOffer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "incoming_trade_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketIncomingTradeOffer> STREAM_CODEC =
            StreamCodec.of(PacketIncomingTradeOffer::encode, PacketIncomingTradeOffer::decode);

    private final TradeOffer offer;

    public PacketIncomingTradeOffer(TradeOffer offer) {
        this.offer = offer;
    }

    public TradeOffer offer() {
        return offer;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketIncomingTradeOffer msg) {
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

    public static PacketIncomingTradeOffer decode(RegistryFriendlyByteBuf buf) {
        UUID offerId = buf.readUUID();
        UUID senderUuid = buf.readUUID();
        String senderName = buf.readUtf();
        UUID receiverUuid = buf.readUUID();
        String receiverName = buf.readUtf();
        List<net.minecraft.resources.Identifier> senderHats = TradePacketBuffers.readIds(buf);
        List<net.minecraft.resources.Identifier> receiverHats = TradePacketBuffers.readIds(buf);
        long createdAt = buf.readLong();
        return new PacketIncomingTradeOffer(new TradeOffer(
                offerId, senderUuid, senderName, receiverUuid, receiverName, senderHats, receiverHats, createdAt));
    }

    public static void handle(PacketIncomingTradeOffer msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                com.astryxion.hats.client.trade.ClientTradePackets.onIncomingOffer(msg);
            }
        });
    }
}
