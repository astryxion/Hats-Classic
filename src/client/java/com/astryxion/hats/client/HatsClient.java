package com.astryxion.hats.client;

import com.astryxion.hats.client.gui.HatScreen;
import com.astryxion.hats.client.keybinds.HatKeybinds;
import com.astryxion.hats.client.network.HatClientPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;

public class HatsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HatClientRenderSetup.register();
        KeyMappingHelper.registerKeyMapping(HatKeybinds.OPEN_HATS);
        HatClientPackets.registerS2CReceiver();
        ClientTickEvents.END_CLIENT_TICK.register(HatsClient::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        if (HatKeybinds.OPEN_HATS == null) return;
        while (HatKeybinds.OPEN_HATS.consumeClick()) {
            if (client.player == null) return;
            if (client.screen == null) {
                client.setScreen(new HatScreen());
            }
        }
    }
}
