package com.astryxion.hats.common.trade;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeOffer {

    public static final long EXPIRY_MS = 5L * 60L * 1000L;

    public final UUID offerId;
    public final UUID senderUuid;
    public final String senderName;
    public final UUID receiverUuid;
    public final String receiverName;
    public final List<ResourceLocation> senderHats;
    public final List<ResourceLocation> receiverHats;
    public final long createdAtMs;

    public TradeOffer(
            UUID offerId,
            UUID senderUuid,
            String senderName,
            UUID receiverUuid,
            String receiverName,
            List<ResourceLocation> senderHats,
            List<ResourceLocation> receiverHats,
            long createdAtMs
    ) {
        this.offerId = offerId;
        this.senderUuid = senderUuid;
        this.senderName = senderName;
        this.receiverUuid = receiverUuid;
        this.receiverName = receiverName;
        this.senderHats = List.copyOf(senderHats);
        this.receiverHats = List.copyOf(receiverHats);
        this.createdAtMs = createdAtMs;
    }

    public boolean isExpired(long nowMs) {
        return nowMs - createdAtMs >= EXPIRY_MS;
    }

    public long remainingMs(long nowMs) {
        return Math.max(0L, EXPIRY_MS - (nowMs - createdAtMs));
    }

    public static List<ResourceLocation> copyList(List<ResourceLocation> src) {
        return new ArrayList<>(src);
    }
}
