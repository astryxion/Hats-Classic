package com.astryxion.hats.client.trade;

import com.astryxion.hats.Hats;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT)
public final class ClientTradeEvents {

    private ClientTradeEvents() {}

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientTradeState.reset();
        com.astryxion.hats.client.hat.ClientHatUnlocks.clear();
    }
}
