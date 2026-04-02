package com.astryxion.astryxionshats.common.capability;

import com.astryxion.astryxionshats.common.hat.HatPartCapability;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;
import net.minecraft.server.level.ServerPlayer;

public class HatLoginHandler {

    public static void onPlayerLogin(ServerPlayer player) {
        var part = HatPartCapability.get(player);
        if (part != null) {
            HatPacketHandler.sendSyncHatPartToPlayer(
                    player,
                    player.getId(),
                    part.serializeNBT(player.getServer().registryAccess())
            );
        }
    }
}
