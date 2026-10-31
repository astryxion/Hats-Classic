package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.server.level.ServerPlayer;

public final class HatTradeNetwork {

    private HatTradeNetwork() {}

    public static void send(ServerPlayer player, Object packet) {
        if (player == null) {
            return;
        }
        HatPacketHandler.sendToPlayer(player, packet);
        HatPacketHandler.callClient(
                "com.astryxion.hats.client.trade.ClientTradePackets",
                "deliverIfLocalHost",
                new Class<?>[]{ServerPlayer.class, Object.class},
                new Object[]{player, packet});
    }
}
