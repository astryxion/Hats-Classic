package com.astryxion.hats.common.capability;

import com.astryxion.hats.common.network.HatPacketHandler;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * When a client starts tracking another player, send that player's hat render state.
 * Without this, join order can leave remote players with empty hat part data on the client.
 */
public final class PlayerHatTrackingSync {

    private PlayerHatTrackingSync() {}

    public static void register() {
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, trackingPlayer) -> {
            if (!(trackingPlayer instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer tracker = (ServerPlayer) trackingPlayer;
            if (!(trackedEntity instanceof Player)) {
                return;
            }
            Player tracked = (Player) trackedEntity;
            if (tracked == tracker) {
                return;
            }
            HatPacketHandler.sendPlayerHatPartTo(tracker, tracked);
        });
    }
}
