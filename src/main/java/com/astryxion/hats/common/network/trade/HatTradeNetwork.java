package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class HatTradeNetwork {

    private HatTradeNetwork() {}

    public static void send(ServerPlayer player, Object packet) {
        if (player == null) {
            return;
        }
        HatPacketHandler.sendToPlayer(player, packet);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.astryxion.hats.client.trade.ClientTradePackets.deliverIfLocalHost(player, packet));
    }
}
