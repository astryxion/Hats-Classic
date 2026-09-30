package com.astryxion.hats.client.trade;

import com.astryxion.hats.client.gui.toast.TradeRequestToast;
import com.astryxion.hats.common.network.trade.PacketIncomingTradeOffer;
import com.astryxion.hats.common.network.trade.PacketNearbyPlayersList;
import com.astryxion.hats.common.network.trade.PacketPeerInventory;
import com.astryxion.hats.common.network.trade.PacketTradeError;
import com.astryxion.hats.common.network.trade.PacketTradeOfferRevoked;
import com.astryxion.hats.common.network.trade.PacketTradePartnerLeft;
import com.astryxion.hats.common.network.trade.PacketTradeResolved;
import com.astryxion.hats.common.trade.TradeOffer;

public final class ClientTradePackets {

    private ClientTradePackets() {}

    public static void onNearbyPlayers(PacketNearbyPlayersList msg) {
        ClientTradeState.setNearbyPlayers(msg.players());
    }

    public static void onPeerInventory(PacketPeerInventory msg) {
        ClientTradeState.setPeerInventory(msg.owner(), msg.hats(), msg.equipped());
    }

    public static void onIncomingOffer(PacketIncomingTradeOffer msg) {
        TradeOffer offer = msg.offer();
        if (ClientTradeState.getIncomingOffer(offer.offerId) != null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - offer.createdAtMs;
        long localCreated = System.currentTimeMillis() - Math.max(0L, elapsed);
        ClientTradeState.addIncomingOffer(new ClientTradeState.IncomingOffer(
                offer.offerId,
                offer.senderUuid,
                offer.senderName,
                offer.senderHats,
                offer.receiverHats,
                localCreated
        ));
        TradeRequestToast.show(offer.senderName);
    }

    public static void onResolved(PacketTradeResolved msg) {
        ClientTradeState.removeIncomingOffer(msg.offerId());
        if (msg.accepted()) {
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            com.astryxion.hats.client.hat.ClientHatUnlocks.ensureSeeded(minecraft.player);
            ClientTradeState.PendingOutgoing pending = ClientTradeState.takePendingOutgoing();
            if (pending != null) {
                com.astryxion.hats.client.hat.ClientHatUnlocks.applySwap(pending.gained, pending.lost);
            }
            com.astryxion.hats.client.hat.ClientHatUnlocks.applySwap(msg.gained(), msg.lost());
        } else {
            ClientTradeState.takePendingOutgoing();
        }
        ClientTradeFeedback.onResolved(msg.accepted(), msg.otherName());
    }

    public static void onRevoked(PacketTradeOfferRevoked msg) {
        ClientTradeState.removeIncomingOffer(msg.offerId());
    }

    public static void onPartnerLeft(PacketTradePartnerLeft msg) {
        ClientTradeFlow.onPartnerLeft(msg.targetUuid());
    }

    public static void onError(PacketTradeError msg) {
        ClientTradeState.setError(msg.type(), msg.details());
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player != null) {
            net.minecraft.network.chat.Component text = net.minecraft.network.chat.Component
                    .translatable(msg.type().messageKey())
                    .withStyle(net.minecraft.ChatFormatting.RED);
            minecraft.player.displayClientMessage(text, false);
            minecraft.player.displayClientMessage(text, true);
        }
        com.astryxion.hats.client.gui.HatsGui.open();
    }

    /**
     * Integrated-server host often never receives its own S2C packets. Apply
     * them on the client thread when the local player is the target.
     */
    public static void deliverIfLocalHost(net.minecraft.server.level.ServerPlayer player, Object packet) {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        java.util.UUID target = player.getUUID();
        String targetName = player.getGameProfile().getName();
        minecraft.execute(() -> {
            if (minecraft.player == null) {
                return;
            }
            if (!minecraft.player.getUUID().equals(target)
                    && !minecraft.player.getGameProfile().getName().equalsIgnoreCase(targetName)) {
                return;
            }
            if (packet instanceof PacketIncomingTradeOffer incoming) {
                onIncomingOffer(incoming);
            } else if (packet instanceof PacketTradeResolved resolved) {
                onResolved(resolved);
            } else if (packet instanceof PacketTradeOfferRevoked revoked) {
                onRevoked(revoked);
            } else if (packet instanceof PacketTradePartnerLeft left) {
                onPartnerLeft(left);
            } else if (packet instanceof PacketTradeError error) {
                onError(error);
            }
        });
    }
}
