package com.astryxion.hats.common.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TradeOfferStore {

    public static final double TRADE_RANGE_BLOCKS = 4.0;

    private static final TradeOfferStore INSTANCE = new TradeOfferStore();

    public static TradeOfferStore get() {
        return INSTANCE;
    }

    private final Map<UUID, TradeOffer> offers = new HashMap<>();
    private final Map<UUID, Set<UUID>> bySender = new HashMap<>();
    private final Map<UUID, Set<UUID>> byReceiver = new HashMap<>();
    private final Map<UUID, TradeBuilderSession> sessions = new HashMap<>();

    public enum CreateResult {
        OK,
        DUPLICATE
    }

    public TradeOffer addOrReplace(TradeOffer offer) {
        TradeOffer existing = findPair(offer.senderUuid, offer.receiverUuid);
        if (existing != null) {
            remove(existing.offerId);
        }
        offers.put(offer.offerId, offer);
        bySender.computeIfAbsent(offer.senderUuid, key -> new HashSet<>()).add(offer.offerId);
        byReceiver.computeIfAbsent(offer.receiverUuid, key -> new HashSet<>()).add(offer.offerId);
        return existing;
    }

    public CreateResult addOffer(TradeOffer offer) {
        if (findPair(offer.senderUuid, offer.receiverUuid) != null) {
            return CreateResult.DUPLICATE;
        }
        addOrReplace(offer);
        return CreateResult.OK;
    }

    public TradeOffer get(UUID offerId) {
        return offers.get(offerId);
    }

    public TradeOffer findPair(UUID sender, UUID receiver) {
        Set<UUID> senderOffers = bySender.get(sender);
        if (senderOffers == null) {
            return null;
        }
        for (UUID id : senderOffers) {
            TradeOffer offer = offers.get(id);
            if (offer != null && offer.receiverUuid.equals(receiver)) {
                return offer;
            }
        }
        return null;
    }

    public TradeOffer remove(UUID offerId) {
        TradeOffer offer = offers.remove(offerId);
        if (offer != null) {
            removeIndex(bySender, offer.senderUuid, offerId);
            removeIndex(byReceiver, offer.receiverUuid, offerId);
        }
        return offer;
    }

    public List<TradeOffer> getOffersForReceiver(UUID receiver) {
        Set<UUID> ids = byReceiver.get(receiver);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<TradeOffer> out = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            TradeOffer offer = offers.get(id);
            if (offer != null) {
                out.add(offer);
            }
        }
        return out;
    }

    public List<TradeOffer> removeExpired(long nowMs) {
        List<TradeOffer> expired = new ArrayList<>();
        Iterator<Map.Entry<UUID, TradeOffer>> it = offers.entrySet().iterator();
        while (it.hasNext()) {
            TradeOffer offer = it.next().getValue();
            if (offer.isExpired(nowMs)) {
                expired.add(offer);
                it.remove();
                removeIndex(bySender, offer.senderUuid, offer.offerId);
                removeIndex(byReceiver, offer.receiverUuid, offer.offerId);
            }
        }
        return expired;
    }

    public List<TradeOffer> removeAllInvolving(UUID playerUuid) {
        List<TradeOffer> removed = new ArrayList<>();
        Set<UUID> ids = new HashSet<>();
        Set<UUID> sent = bySender.get(playerUuid);
        Set<UUID> received = byReceiver.get(playerUuid);
        if (sent != null) {
            ids.addAll(sent);
        }
        if (received != null) {
            ids.addAll(received);
        }
        for (UUID id : ids) {
            TradeOffer offer = remove(id);
            if (offer != null) {
                removed.add(offer);
            }
        }
        return removed;
    }

    public void openSession(TradeBuilderSession session) {
        sessions.put(session.initiatorUuid, session);
    }

    public TradeBuilderSession closeSession(UUID initiatorUuid) {
        return sessions.remove(initiatorUuid);
    }

    public TradeBuilderSession getSession(UUID initiatorUuid) {
        return sessions.get(initiatorUuid);
    }

    public java.util.Collection<TradeBuilderSession> allSessions() {
        return new ArrayList<>(sessions.values());
    }

    private static void removeIndex(Map<UUID, Set<UUID>> index, UUID player, UUID offerId) {
        Set<UUID> ids = index.get(player);
        if (ids == null) {
            return;
        }
        ids.remove(offerId);
        if (ids.isEmpty()) {
            index.remove(player);
        }
    }
}
