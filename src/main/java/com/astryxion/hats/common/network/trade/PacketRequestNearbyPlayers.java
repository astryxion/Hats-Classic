package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.trade.TradeOfferStore;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class PacketRequestNearbyPlayers implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketRequestNearbyPlayers> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "request_nearby_players"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestNearbyPlayers> STREAM_CODEC =
            StreamCodec.of(PacketRequestNearbyPlayers::encode, PacketRequestNearbyPlayers::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketRequestNearbyPlayers msg) {
    }

    public static PacketRequestNearbyPlayers decode(RegistryFriendlyByteBuf buf) {
        return new PacketRequestNearbyPlayers();
    }

    public static void handle(PacketRequestNearbyPlayers msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) ctx.player();
            if (sender == null || sender.level().getServer() == null) {
                return;
            }
            List<PacketNearbyPlayersList.Entry> nearby = new ArrayList<>();
            double range = TradeOfferStore.TRADE_RANGE_BLOCKS;
            double rangeSq = range * range;
            for (ServerPlayer other : sender.level().getServer().getPlayerList().getPlayers()) {
                if (other == sender || other.level() != sender.level()) {
                    continue;
                }
                if (other.distanceToSqr(sender) > rangeSq) {
                    continue;
                }
                nearby.add(new PacketNearbyPlayersList.Entry(other.getUUID(), other.getGameProfile().name()));
            }
            HatTradeNetwork.send(sender, new PacketNearbyPlayersList(nearby));
        });
    }
}
