package com.astryxion.hats.common.capability;

import com.astryxion.hats.common.network.HatPacketHandler;
import net.minecraft.server.level.ServerPlayer;

public class HatLoginHandler {

    public static void onPlayerLogin(ServerPlayer player) {
        HatPacketHandler.syncPlayerHatPartToTracking(player);
    }
}
