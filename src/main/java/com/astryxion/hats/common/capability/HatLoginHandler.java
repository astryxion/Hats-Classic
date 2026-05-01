package com.astryxion.hats.common.capability;

import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.network.HatPacketHandler;
import net.minecraft.server.level.ServerPlayer;

public class HatLoginHandler {

    public static void onPlayerLogin(ServerPlayer player) {
        var server = player.level().getServer();
        if (server == null) {
            return;
        }

        // Joining client: apply every other online player's hat (they only had local data before).
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other == player) {
                continue;
            }
            var otherPart = HatPartCapability.get(other);
            if (otherPart != null) {
                HatPacketHandler.sendSyncHatPartToPlayer(
                        player,
                        other.getId(),
                        otherPart.serializeNBT(player.registryAccess()));
            }
        }

        // Everyone tracking this player (and self): this player's hat.
        var part = HatPartCapability.get(player);
        if (part != null) {
            HatPacketHandler.sendSyncHatPartToTracking(
                    player,
                    player.getId(),
                    part.serializeNBT(player.registryAccess()));
        }
    }
}
