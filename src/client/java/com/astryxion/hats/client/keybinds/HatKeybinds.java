package com.astryxion.hats.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class HatKeybinds {

    public static KeyMapping OPEN_HATS = new KeyMapping(
            "key.hats.open_gui",
            InputConstants.KEY_H,
            KeyMapping.Category.MISC
    );
}
