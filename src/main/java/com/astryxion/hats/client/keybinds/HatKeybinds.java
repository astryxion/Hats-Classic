package com.astryxion.hats.client.keybinds;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;
import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.gui.modern.ModernGuiCamera;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

public class HatKeybinds {

    public static KeyMapping OPEN_HATS;

    // =========================
    // MOD BUS -> register key
    // =========================

    @EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT)
    public static class Register {

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {

            OPEN_HATS = new KeyMapping(
                    "key.hats.open_gui",
                    GLFW.GLFW_KEY_H,
                    KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Hats.MODID, "hats"))
            );

            event.register(OPEN_HATS);
        }
    }

    // =========================
    // FORGE BUS -> listen press
    // =========================

    @EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT)
    public static class Listener {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {

            ModernGuiCamera.tick();

            if (OPEN_HATS == null) return;

            if (OPEN_HATS.consumeClick()) {

                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null) return;

                if (mc.screen == null) {
                    ModernGuiCamera.resetOrbit();
                    HatsGui.open();
                }
            }
        }
    }
}
