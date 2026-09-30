package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.capability.HatDataAccess;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.trade.TradeErrorType;
import com.astryxion.hats.common.trade.TradeHats;
import com.astryxion.hats.common.trade.TradeOffer;
import com.astryxion.hats.common.trade.TradeOfferStore;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketRespondTradeOffer {

    private final UUID offerId;
    private final boolean accepted;

    public PacketRespondTradeOffer(UUID offerId, boolean accepted) {
        this.offerId = offerId;
        this.accepted = accepted;
    }

    public static void encode(PacketRespondTradeOffer msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.offerId);
        buf.writeBoolean(msg.accepted);
    }

    public static PacketRespondTradeOffer decode(FriendlyByteBuf buf) {
        return new PacketRespondTradeOffer(buf.readUUID(), buf.readBoolean());
    }

    public static void handle(PacketRespondTradeOffer msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer responder = context.getSender();
            try {
                resolve(msg, responder);
            } catch (Throwable t) {
                com.astryxion.hats.Hats.LOGGER.error("Hat trade accept failed for {}", msg.offerId, t);
                if (responder != null) {
                    HatTradeNetwork.send(responder, new PacketTradeError(TradeErrorType.OFFER_NOT_FOUND, ""));
                    HatTradeNetwork.send(responder, new PacketTradeOfferRevoked(msg.offerId));
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static void resolve(PacketRespondTradeOffer msg, ServerPlayer responder) {
        if (responder == null || responder.getServer() == null) {
            return;
        }
            TradeOffer offer = TradeOfferStore.get().get(msg.offerId);
            if (offer == null) {
                HatTradeNetwork.send(responder, new PacketTradeError(TradeErrorType.OFFER_NOT_FOUND, ""));
                return;
            }
            if (!offer.receiverUuid.equals(responder.getUUID())
                    && !offer.receiverName.equalsIgnoreCase(responder.getGameProfile().getName())) {
                HatTradeNetwork.send(responder, new PacketTradeError(TradeErrorType.OFFER_NOT_FOUND, ""));
                return;
            }
            if (offer.isExpired(System.currentTimeMillis())) {
                TradeOfferStore.get().remove(msg.offerId);
                HatTradeNetwork.send(responder, new PacketTradeError(TradeErrorType.OFFER_EXPIRED, ""));
                HatTradeNetwork.send(responder, new PacketTradeOfferRevoked(msg.offerId));
                return;
            }

            ServerPlayer sender = responder.getServer().getPlayerList().getPlayer(offer.senderUuid);
            if (sender == null) {
                for (ServerPlayer online : responder.getServer().getPlayerList().getPlayers()) {
                    if (offer.senderName.equalsIgnoreCase(online.getGameProfile().getName())) {
                        sender = online;
                        break;
                    }
                }
            }
            if (!msg.accepted) {
                TradeOfferStore.get().remove(msg.offerId);
                HatTradeNetwork.send(responder, new PacketTradeOfferRevoked(msg.offerId));
                if (sender != null) {
                    HatTradeNetwork.send(sender, new PacketTradeResolved(msg.offerId, false, offer.receiverName));
                }
                return;
            }

            if (sender == null || sender.level() != responder.level()) {
                TradeOfferStore.get().remove(msg.offerId);
                HatTradeNetwork.send(responder, new PacketTradeError(TradeErrorType.RECEIVER_OFFLINE, ""));
                HatTradeNetwork.send(responder, new PacketTradeOfferRevoked(msg.offerId));
                return;
            }
            if (!TradeHats.inRange(sender, responder)) {
                HatTradeNetwork.send(responder, new PacketTradeError(TradeErrorType.OUT_OF_RANGE, ""));
                return;
            }

            com.astryxion.hats.common.data.ServerHatUnlocks store =
                    com.astryxion.hats.common.data.ServerHatUnlocks.get(sender);
            store.ensureSeeded(sender);
            store.ensureSeeded(responder);

            ResourceLocation senderEquipped = TradeHats.equippedId(sender);
            ResourceLocation receiverEquipped = TradeHats.equippedId(responder);
            if (!stillOwned(responder, sender, offer, sender, store, offer.senderHats, true)
                    || !stillOwned(responder, sender, offer, responder, store, offer.receiverHats, false)) {
                return;
            }

            ResourceLocation senderSavedEquipped = senderEquipped;
            ResourceLocation receiverSavedEquipped = receiverEquipped;
            if (senderEquipped != null && offer.senderHats.contains(senderEquipped)
                    && store.count(sender, senderEquipped) <= 1) {
                HatManager.clearHat(sender);
                senderSavedEquipped = null;
            }
            if (receiverEquipped != null && offer.receiverHats.contains(receiverEquipped)
                    && store.count(responder, receiverEquipped) <= 1) {
                HatManager.clearHat(responder);
                receiverSavedEquipped = null;
            }

            for (ResourceLocation id : offer.senderHats) {
                store.take(sender, id);
                store.add(responder, id);
            }
            for (ResourceLocation id : offer.receiverHats) {
                store.take(responder, id);
                store.add(sender, id);
            }

            TradeOfferStore.get().remove(msg.offerId);
            List<ResourceLocation> senderUnlocked = List.copyOf(store.copy(sender));
            List<ResourceLocation> receiverUnlocked = List.copyOf(store.copy(responder));
            HatPacketHandler.syncPlayerHatPartToTracking(sender);
            HatPacketHandler.syncPlayerHatPartToTracking(responder);
            HatTradeNetwork.send(sender, new PacketTradeResolved(
                    msg.offerId, true, offer.receiverName, senderUnlocked,
                    offer.receiverHats, offer.senderHats, senderSavedEquipped));
            HatTradeNetwork.send(responder, new PacketTradeResolved(
                    msg.offerId, true, offer.senderName, receiverUnlocked,
                    offer.senderHats, offer.receiverHats, receiverSavedEquipped));
            com.astryxion.hats.common.network.PacketSetUnlockedHats.sendTrade(
                    sender, senderUnlocked, senderSavedEquipped, offer.receiverHats, offer.senderHats);
            com.astryxion.hats.common.network.PacketSetUnlockedHats.sendTrade(
                    responder, receiverUnlocked, receiverSavedEquipped, offer.senderHats, offer.receiverHats);
            HatTradeNetwork.send(responder, new PacketTradeOfferRevoked(msg.offerId));
            com.astryxion.hats.Hats.LOGGER.info(
                    "Hat trade accepted: {} -> {} exchanged {} for {}",
                    offer.senderName, offer.receiverName, offer.senderHats, offer.receiverHats);
    }

    private static boolean stillOwned(
            ServerPlayer responder,
            ServerPlayer sender,
            TradeOffer offer,
            ServerPlayer owner,
            com.astryxion.hats.common.data.ServerHatUnlocks store,
            List<ResourceLocation> hats,
            boolean senderSide
    ) {
        for (ResourceLocation id : hats) {
            if (!TradeHats.isHat(id)
                    || (Config.hatMode != HatMode.COSMETIC
                    && !store.has(owner, id)
                    && !HatDataAccess.has(owner, id))) {
                revokeAndError(responder, sender, offer,
                        senderSide ? TradeErrorType.SENDER_MISSING_HAT : TradeErrorType.RECEIVER_MISSING_HAT,
                        id.toString());
                return false;
            }
        }
        return true;
    }

    private static void revokeAndError(
            ServerPlayer responder,
            ServerPlayer sender,
            TradeOffer offer,
            TradeErrorType type,
            String detail
    ) {
        TradeOfferStore.get().remove(offer.offerId);
        HatTradeNetwork.send(responder, new PacketTradeError(type, detail));
        HatTradeNetwork.send(responder, new PacketTradeOfferRevoked(offer.offerId));
        if (sender != null) {
            HatTradeNetwork.send(sender, new PacketTradeError(type, detail));
        }
    }
}
