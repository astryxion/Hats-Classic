package com.astryxion.astryxionshats.client.keybinds;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class HatKeybinds {

    public static KeyMapping OPEN_HATS = new KeyMapping(
            "key.astryxionshats.open_gui",
            com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.astryxionshats"
    );
}
