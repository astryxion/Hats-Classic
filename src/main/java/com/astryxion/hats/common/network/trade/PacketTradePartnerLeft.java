package com.astryxion.hats.common.network.trade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketTradePartnerLeft implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketTradePartnerLeft> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "trade_partner_left"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTradePartnerLeft> STREAM_CODEC =
            StreamCodec.of(PacketTradePartnerLeft::encode, PacketTradePartnerLeft::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final UUID targetUuid;

    public PacketTradePartnerLeft(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketTradePartnerLeft msg) {
        buf.writeUUID(msg.targetUuid);
    }

    public static PacketTradePartnerLeft decode(RegistryFriendlyByteBuf buf) {
        return new PacketTradePartnerLeft(buf.readUUID());
    }

    public static void handle(PacketTradePartnerLeft msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.trade.ClientTradePackets",
                "onPartnerLeft",
                new Class<?>[]{PacketTradePartnerLeft.class},
                new Object[]{msg}));
        
    }
}
