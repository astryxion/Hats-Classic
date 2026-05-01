package com.astryxion.hats.client;

import com.astryxion.hats.client.keybinds.HatKeybinds;
import net.fabricmc.api.ClientModInitializer;

public class AstryxionsHatsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HatKeybinds.register();
    }
}
