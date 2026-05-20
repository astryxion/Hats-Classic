package com.astryxion.hats.common.capability;

import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.hat.HatPartRestore;
import com.astryxion.hats.common.network.HatPacketHandler;
import net.minecraft.server.level.ServerPlayer;

public class HatLoginHandler {

    public static void onPlayerLogin(ServerPlayer player) {
        var server = player.level().getServer();
        if (server == null) {
            return;
        }

        // HatPart is in-memory only; restore everyone's equipped visuals from SavedData before sync.
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            HatPartRestore.restoreFromSavedData(online);
        }

        // Joining client: apply every other online player's hat.
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other == player) {
                continue;
            }
            sendPartTo(player, other);
        }

        // Everyone tracking this player (and self): this player's hat.
        sendPartToTracking(player);
    }

    private static void sendPartTo(ServerPlayer recipient, ServerPlayer hatOwner) {
        var part = HatPartCapability.getOrCreate(hatOwner);
        HatPacketHandler.sendSyncHatPartToPlayer(
                recipient,
                hatOwner.getId(),
                part.serializeNBT(recipient.registryAccess()));
    }

    private static void sendPartToTracking(ServerPlayer owner) {
        var part = HatPartCapability.getOrCreate(owner);
        HatPacketHandler.sendSyncHatPartToTracking(
                owner,
                owner.getId(),
                part.serializeNBT(owner.registryAccess()));
    }
}
