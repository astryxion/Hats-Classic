package com.astryxion.hats.client;

import com.astryxion.hats.client.gui.HatScreen;
import com.astryxion.hats.client.keybinds.HatKeybinds;
import com.astryxion.hats.client.network.PacketHatUnlockedClient;
import com.astryxion.hats.common.network.HatPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;

public class HatsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerClient();
    }

    public static void registerClient() {
        HatPacketHandler.bindHatUnlockedClientHandler(PacketHatUnlockedClient::handle);
        KeyBindingHelper.registerKeyBinding(HatKeybinds.OPEN_HATS);
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
