package com.astryxion.hats.client.keybinds;

import com.astryxion.hats.Hats;
import com.astryxion.hats.client.gui.HatScreen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

public class HatKeybinds {

    public static KeyMapping OPEN_HATS;

    // =========================
    // MOD BUS -> register key
    // =========================

    @Mod.EventBusSubscriber(modid = Hats.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Register {

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {

            OPEN_HATS = new KeyMapping(
                    "key.hats.open_gui",
                    GLFW.GLFW_KEY_H,
                    "key.categories.hats"
            );

            event.register(OPEN_HATS);
        }
    }

    // =========================
    // FORGE BUS -> listen press
    // =========================

    @Mod.EventBusSubscriber(modid = Hats.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class Listener {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {

            if (event.phase != TickEvent.Phase.END) return;

            if (OPEN_HATS == null) return;

            if (OPEN_HATS.consumeClick()) {

                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null) return;

                if (mc.screen == null) {
                    mc.setScreen(new HatScreen());
                }
            }
        }
    }
}
