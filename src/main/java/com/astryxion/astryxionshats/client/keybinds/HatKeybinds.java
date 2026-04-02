package com.astryxion.astryxionshats.client.keybinds;

import com.astryxion.astryxionshats.client.gui.HatScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class HatKeybinds {

    public static KeyMapping OPEN_HATS;

    public static void register() {
        OPEN_HATS = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.astryxionshats.open_gui",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.astryxionshats"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_HATS == null) return;
            while (OPEN_HATS.consumeClick()) {
                if (client.player == null) return;
                if (client.screen == null) {
                    client.setScreen(new HatScreen());
                }
            }
        });
    }
}
