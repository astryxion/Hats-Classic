package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public final class HatTradeNetwork {

    private HatTradeNetwork() {}

    public static void send(ServerPlayer player, Object packet) {
        if (player == null) {
            return;
        }
        Object outbound = packet instanceof PacketTradeError error ? new PacketTradeError.Payload(error) : packet;
        HatPacketHandler.sendToPlayer(player, outbound);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            com.astryxion.hats.client.trade.ClientTradePackets.deliverIfLocalHost(player, packet);
        }
    }
}
