package com.astryxion.hats.client.trade;

import com.astryxion.hats.common.network.trade.PacketNearbyPlayersList;
import com.astryxion.hats.common.trade.TradeErrorType;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientTradeState {

    public static final class IncomingOffer {
        public final UUID offerId;
        public final UUID senderUuid;
        public final String senderName;
        public final List<ResourceLocation> senderHats;
        public final List<ResourceLocation> receiverHats;
        public final long createdAtClientMs;

        public IncomingOffer(
                UUID offerId,
                UUID senderUuid,
                String senderName,
                List<ResourceLocation> senderHats,
                List<ResourceLocation> receiverHats,
                long createdAtClientMs
        ) {
            this.offerId = offerId;
            this.senderUuid = senderUuid;
            this.senderName = senderName;
            this.senderHats = List.copyOf(senderHats);
            this.receiverHats = List.copyOf(receiverHats);
            this.createdAtClientMs = createdAtClientMs;
        }
    }

    private static List<PacketNearbyPlayersList.Entry> nearby = new ArrayList<>();
    private static boolean nearbyReceived;
    private static final Map<UUID, List<ResourceLocation>> peerInventories = new LinkedHashMap<>();
    private static final Map<UUID, ResourceLocation> peerEquipped = new LinkedHashMap<>();
    private static final Map<UUID, IncomingOffer> incomingOffers = new LinkedHashMap<>();
    @Nullable
    private static PendingOutgoing pendingOutgoing;
    @Nullable
    private static UUID activeBuilderTarget;
    @Nullable
    private static TradeErrorType pendingError;
    @Nullable
    private static String pendingErrorDetails;

    private ClientTradeState() {}

    public static void setNearbyPlayers(List<PacketNearbyPlayersList.Entry> players) {
        nearby = new ArrayList<>(players);
        nearbyReceived = true;
    }

    public static void clearNearby() {
        nearby = new ArrayList<>();
        nearbyReceived = false;
    }

    public static List<PacketNearbyPlayersList.Entry> getNearby() {
        return Collections.unmodifiableList(nearby);
    }

    public static boolean isNearbyReceived() {
        return nearbyReceived;
    }

    public static void setPeerInventory(UUID owner, List<ResourceLocation> hats, @Nullable ResourceLocation equipped) {
        peerInventories.put(owner, List.copyOf(hats));
        if (equipped == null) {
            peerEquipped.remove(owner);
        } else {
            peerEquipped.put(owner, equipped);
        }
    }

    @Nullable
    public static List<ResourceLocation> getPeerInventory(UUID owner) {
        return peerInventories.get(owner);
    }

    @Nullable
    public static ResourceLocation getPeerEquipped(UUID owner) {
        return peerEquipped.get(owner);
    }

    public static void addIncomingOffer(IncomingOffer offer) {
        incomingOffers.put(offer.offerId, offer);
    }

    public static void removeIncomingOffer(UUID offerId) {
        incomingOffers.remove(offerId);
    }

    public static java.util.Collection<IncomingOffer> getIncomingOffers() {
        return Collections.unmodifiableCollection(incomingOffers.values());
    }

    @Nullable
    public static IncomingOffer getIncomingOffer(UUID offerId) {
        return incomingOffers.get(offerId);
    }

    public static boolean hasIncomingOffers() {
        return !incomingOffers.isEmpty();
    }

    public static void setActiveBuilderTarget(@Nullable UUID target) {
        activeBuilderTarget = target;
    }

    @Nullable
    public static UUID getActiveBuilderTarget() {
        return activeBuilderTarget;
    }

    public static void setError(TradeErrorType type, @Nullable String details) {
        pendingError = type;
        pendingErrorDetails = details;
    }

    @Nullable
    public static TradeErrorType consumeError() {
        TradeErrorType error = pendingError;
        pendingError = null;
        return error;
    }

    public static void setPendingOutgoing(Collection<ResourceLocation> lost, Collection<ResourceLocation> gained) {
        pendingOutgoing = new PendingOutgoing(lost, gained);
    }

    @Nullable
    public static PendingOutgoing takePendingOutgoing() {
        PendingOutgoing pending = pendingOutgoing;
        pendingOutgoing = null;
        return pending;
    }

    public static void reset() {
        nearby = new ArrayList<>();
        nearbyReceived = false;
        peerInventories.clear();
        peerEquipped.clear();
        incomingOffers.clear();
        pendingOutgoing = null;
        activeBuilderTarget = null;
        pendingError = null;
        pendingErrorDetails = null;
    }

    public static final class PendingOutgoing {
        public final List<ResourceLocation> lost;
        public final List<ResourceLocation> gained;

        private PendingOutgoing(Collection<ResourceLocation> lost, Collection<ResourceLocation> gained) {
            this.lost = List.copyOf(lost);
            this.gained = List.copyOf(gained);
        }
    }
}
