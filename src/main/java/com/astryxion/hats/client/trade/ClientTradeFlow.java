package com.astryxion.hats.client.trade;

import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.gui.trade.TradeIncomingScreen;
import com.astryxion.hats.client.gui.trade.TradeOfferScreen;
import com.astryxion.hats.client.gui.trade.TradePartnerSelectScreen;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.trade.PacketCloseTradeBuilder;
import com.astryxion.hats.common.network.trade.PacketRequestNearbyPlayers;
import com.astryxion.hats.common.network.trade.PacketRequestPeerInventory;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public final class ClientTradeFlow {

    private ClientTradeFlow() {}

    public static void openTrade() {
        var incoming = ClientTradeState.getIncomingOffers();
        if (!incoming.isEmpty()) {
            openIncoming(incoming.iterator().next().offerId);
            return;
        }
        openPartnerSelect();
    }

    public static void openPartnerSelect() {
        ClientTradeState.clearNearby();
        HatPacketHandler.sendToServer(new PacketRequestNearbyPlayers());
        Minecraft.getInstance().setScreen(new TradePartnerSelectScreen());
    }

    public static void openOfferBuilder(UUID targetUuid, String targetName) {
        ClientTradeState.setActiveBuilderTarget(targetUuid);
        HatPacketHandler.sendToServer(new PacketRequestPeerInventory(targetUuid));
        Minecraft.getInstance().setScreen(new TradeOfferScreen(targetUuid, targetName));
    }

    public static void openIncoming(UUID offerId) {
        ClientTradeState.IncomingOffer offer = ClientTradeState.getIncomingOffer(offerId);
        if (offer == null) {
            return;
        }
        Minecraft.getInstance().setScreen(new TradeIncomingScreen(offer));
    }

    public static void closeBuilder() {
        ClientTradeState.setActiveBuilderTarget(null);
        HatPacketHandler.sendToServer(new PacketCloseTradeBuilder());
    }

    public static void onPartnerLeft(UUID targetUuid) {
        UUID active = ClientTradeState.getActiveBuilderTarget();
        if (active == null || !active.equals(targetUuid)) {
            return;
        }
        ClientTradeState.setActiveBuilderTarget(null);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof TradeOfferScreen || minecraft.screen instanceof TradePartnerSelectScreen) {
            HatsGui.open();
            if (minecraft.player != null) {
                minecraft.player.sendOverlayMessage(
                        Component.translatable("hats.trade.partnerLeft").withStyle(ChatFormatting.RED));
            }
        }
    }
}
