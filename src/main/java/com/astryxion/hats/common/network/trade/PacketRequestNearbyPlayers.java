package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.trade.TradeOfferStore;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketRequestNearbyPlayers {

    public static void encode(PacketRequestNearbyPlayers msg, FriendlyByteBuf buf) {
    }

    public static PacketRequestNearbyPlayers decode(FriendlyByteBuf buf) {
        return new PacketRequestNearbyPlayers();
    }

    public static void handle(PacketRequestNearbyPlayers msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null || sender.getServer() == null) {
                return;
            }
            List<PacketNearbyPlayersList.Entry> nearby = new ArrayList<>();
            double range = TradeOfferStore.TRADE_RANGE_BLOCKS;
            double rangeSq = range * range;
            for (ServerPlayer other : sender.getServer().getPlayerList().getPlayers()) {
                if (other == sender || other.level() != sender.level()) {
                    continue;
                }
                if (other.distanceToSqr(sender) > rangeSq) {
                    continue;
                }
                nearby.add(new PacketNearbyPlayersList.Entry(other.getUUID(), other.getGameProfile().getName()));
            }
            HatTradeNetwork.send(sender, new PacketNearbyPlayersList(nearby));
        });
        context.setPacketHandled(true);
    }
}
