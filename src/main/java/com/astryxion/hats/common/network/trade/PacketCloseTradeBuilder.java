package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.trade.TradeOfferStore;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketCloseTradeBuilder implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketCloseTradeBuilder> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "close_trade_builder"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketCloseTradeBuilder> STREAM_CODEC =
            StreamCodec.of(PacketCloseTradeBuilder::encode, PacketCloseTradeBuilder::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketCloseTradeBuilder msg) {
    }

    public static PacketCloseTradeBuilder decode(RegistryFriendlyByteBuf buf) {
        return new PacketCloseTradeBuilder();
    }

    public static void handle(PacketCloseTradeBuilder msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) ctx.player();
            if (sender != null) {
                TradeOfferStore.get().closeSession(sender.getUUID());
            }
        });
    }
}
