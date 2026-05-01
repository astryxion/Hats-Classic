package com.astryxion.hats.common.capability;

import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.network.HatPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class HatLoginHandler {

    public static void onPlayerLogin(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        HatPacketHandler.broadcastSyncHatPartFor(player);
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other == player) {
                continue;
            }
            var part = HatPartCapability.get(other);
            CompoundTag tag = part != null ? part.serializeNBT(server.registryAccess()) : new CompoundTag();
            HatPacketHandler.sendSyncHatPartToPlayer(player, other.getId(), tag);
        }
    }
}
