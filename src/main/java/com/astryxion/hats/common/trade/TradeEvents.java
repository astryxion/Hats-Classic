package com.astryxion.hats.common.trade;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.network.trade.HatTradeNetwork;
import com.astryxion.hats.common.network.trade.PacketTradeOfferRevoked;
import com.astryxion.hats.common.network.trade.PacketTradePartnerLeft;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = Hats.MODID)
public final class TradeEvents {

    private static int tradeTickCounter;

    private TradeEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        long now = System.currentTimeMillis();
        for (TradeOffer expired : TradeOfferStore.get().removeExpired(now)) {
            ServerPlayer sender = server.getPlayerList().getPlayer(expired.senderUuid);
            ServerPlayer receiver = server.getPlayerList().getPlayer(expired.receiverUuid);
            HatTradeNetwork.send(sender, new PacketTradeOfferRevoked(expired.offerId));
            HatTradeNetwork.send(receiver, new PacketTradeOfferRevoked(expired.offerId));
        }

        tradeTickCounter++;
        if (tradeTickCounter < 10) {
            return;
        }
        tradeTickCounter = 0;
        for (TradeBuilderSession session : TradeOfferStore.get().allSessions()) {
            ServerPlayer initiator = server.getPlayerList().getPlayer(session.initiatorUuid);
            ServerPlayer target = server.getPlayerList().getPlayer(session.targetUuid);
            if (initiator == null) {
                TradeOfferStore.get().closeSession(session.initiatorUuid);
                continue;
            }
            if (!TradeHats.inRange(initiator, target)) {
                TradeOfferStore.get().closeSession(session.initiatorUuid);
                HatTradeNetwork.send(initiator, new PacketTradePartnerLeft(session.targetUuid));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().getServer() == null) {
            return;
        }
        java.util.UUID uuid = player.getUUID();
        TradeOfferStore.get().closeSession(uuid);
        for (TradeOffer offer : TradeOfferStore.get().removeAllInvolving(uuid)) {
            ServerPlayer other = player.level().getServer().getPlayerList().getPlayer(
                    offer.senderUuid.equals(uuid) ? offer.receiverUuid : offer.senderUuid);
            if (other != null) {
                HatTradeNetwork.send(other, new PacketTradeOfferRevoked(offer.offerId));
            }
        }
    }
}
