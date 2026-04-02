package com.astryxion.astryxionshats.client;

import com.astryxion.astryxionshats.client.keybinds.HatKeybinds;
import net.fabricmc.api.ClientModInitializer;

public class AstryxionsHatsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HatKeybinds.register();
    }
}
