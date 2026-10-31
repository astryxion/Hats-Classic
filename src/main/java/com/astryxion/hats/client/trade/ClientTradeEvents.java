package com.astryxion.hats.client.trade;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT)
public final class ClientTradeEvents {

    private ClientTradeEvents() {}

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientTradeState.reset();
        com.astryxion.hats.client.hat.ClientHatUnlocks.clear();
    }
}
