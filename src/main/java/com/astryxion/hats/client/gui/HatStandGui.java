package com.astryxion.hats.client.gui;

import com.astryxion.hats.Config;
import com.astryxion.hats.client.gui.modern.ModernGuiCamera;
import com.astryxion.hats.common.hatstand.HatStandGuiStyle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class HatStandGui {

    private HatStandGui() {}

    public static Screen create(int entityId) {
        return Config.hatStandGuiStyle == HatStandGuiStyle.MODERN
                ? new ModernHatsScreen(entityId)
                : new HatScreen(entityId);
    }

    public static void open(int entityId) {
        ModernGuiCamera.resetOrbit();
        Minecraft.getInstance().setScreen(create(entityId));
    }
}
