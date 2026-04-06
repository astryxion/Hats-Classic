package com.astryxion.hats.client;

import com.astryxion.hats.client.gui.HatScreen;
import com.astryxion.hats.client.keybinds.HatKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class HatsClient {

    public static void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(HatsClient::onRegisterKeyMappings);
        modEventBus.addListener((FMLClientSetupEvent e) -> MinecraftForge.EVENT_BUS.addListener(HatsClient::onClientTick));
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(HatKeybinds.OPEN_HATS);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
