package com.astryxion.hats.common.network.trade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketTradeOfferRevoked implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketTradeOfferRevoked> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "trade_offer_revoked"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTradeOfferRevoked> STREAM_CODEC =
            StreamCodec.of(PacketTradeOfferRevoked::encode, PacketTradeOfferRevoked::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final UUID offerId;

    public PacketTradeOfferRevoked(UUID offerId) {
        this.offerId = offerId;
    }

    public UUID offerId() {
        return offerId;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketTradeOfferRevoked msg) {
        buf.writeUUID(msg.offerId);
    }

    public static PacketTradeOfferRevoked decode(RegistryFriendlyByteBuf buf) {
        return new PacketTradeOfferRevoked(buf.readUUID());
    }

    public static void handle(PacketTradeOfferRevoked msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.trade.ClientTradePackets",
                "onRevoked",
                new Class<?>[]{PacketTradeOfferRevoked.class},
                new Object[]{msg}));
        
    }
}
