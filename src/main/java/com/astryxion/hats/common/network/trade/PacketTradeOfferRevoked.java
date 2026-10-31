package com.astryxion.hats.common.network.trade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class PacketTradeOfferRevoked implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketTradeOfferRevoked> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "trade_offer_revoked"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTradeOfferRevoked> STREAM_CODEC =
            StreamCodec.of(PacketTradeOfferRevoked::encode, PacketTradeOfferRevoked::decode);

    private final UUID offerId;

    public PacketTradeOfferRevoked(UUID offerId) {
        this.offerId = offerId;
    }

    public UUID offerId() {
        return offerId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketTradeOfferRevoked msg) {
        buf.writeUUID(msg.offerId);
    }

    public static PacketTradeOfferRevoked decode(RegistryFriendlyByteBuf buf) {
        return new PacketTradeOfferRevoked(buf.readUUID());
    }

    public static void handle(PacketTradeOfferRevoked msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                com.astryxion.hats.client.trade.ClientTradePackets.onRevoked(msg);
            }
        });
    }
}
