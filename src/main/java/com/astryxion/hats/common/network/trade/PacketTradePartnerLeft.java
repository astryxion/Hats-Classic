package com.astryxion.hats.common.network.trade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class PacketTradePartnerLeft implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketTradePartnerLeft> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "trade_partner_left"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTradePartnerLeft> STREAM_CODEC =
            StreamCodec.of(PacketTradePartnerLeft::encode, PacketTradePartnerLeft::decode);

    private final UUID targetUuid;

    public PacketTradePartnerLeft(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketTradePartnerLeft msg) {
        buf.writeUUID(msg.targetUuid);
    }

    public static PacketTradePartnerLeft decode(RegistryFriendlyByteBuf buf) {
        return new PacketTradePartnerLeft(buf.readUUID());
    }

    public static void handle(PacketTradePartnerLeft msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                com.astryxion.hats.client.trade.ClientTradePackets.onPartnerLeft(msg);
            }
        });
    }
}
