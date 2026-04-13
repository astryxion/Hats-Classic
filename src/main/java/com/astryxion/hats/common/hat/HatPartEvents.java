package com.astryxion.hats.common.hat;

import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.network.HatPacketHandler;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles initial data syncing on player join (sync called from main mod).
 */
public final class HatPartEvents {

    private HatPartEvents() {}

    public static void onPlayerJoinLevel(ServerPlayer player) {
        HatDataCapability.get(player).ifPresent(cap -> {
            HatPacketHandler.sendSyncHatToPlayer(player, cap.serializeNBT());
        });
    }
}
