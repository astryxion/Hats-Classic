package com.astryxion.astryxionshats.client;

import com.astryxion.astryxionshats.client.gui.HatScreen;
import com.astryxion.astryxionshats.client.keybinds.HatKeybinds;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class AstryxionsHatsClient {

    public static void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(RegisterKeyMappingsEvent.class, AstryxionsHatsClient::onRegisterKeyMappings);
        modEventBus.addListener(FMLClientSetupEvent.class, e -> NeoForge.EVENT_BUS.addListener(AstryxionsHatsClient::onClientTick));
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(HatKeybinds.OPEN_HATS);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (HatKeybinds.OPEN_HATS == null) return;
        while (HatKeybinds.OPEN_HATS.consumeClick()) {
            if (client.player == null) return;
            if (client.screen == null) {
                client.setScreen(new HatScreen());
            }
        }
    }
}
