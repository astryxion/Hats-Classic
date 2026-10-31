package com.astryxion.hats.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.16.5 iChunUtil BNS minecraft-style button drawing.
 * 1.21 replaced {@code textures/gui/widgets.png} with nine-slice widget sprites.
 * Idle is the normal button, hover is the highlighted button, and click is the
 * disabled button, matching the old rows at y=66, y=86, and y=46.
 */
public final class HatsWidgets {

    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation BUTTON_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
    private static final ResourceLocation BUTTON_DISABLED = ResourceLocation.withDefaultNamespace("widget/button_disabled");

    public enum ButtonState {
        IDLE,
        HOVER,
        CLICK
    }

    private HatsWidgets() {}

    public static void renderMinecraftStyleButton(GuiGraphics gfx, int x, int y, int width, int height, ButtonState state) {
        ResourceLocation sprite = switch (state) {
            case HOVER -> BUTTON_HIGHLIGHTED;
            case CLICK -> BUTTON_DISABLED;
            case IDLE -> BUTTON;
        };
        gfx.blitSprite(sprite, x, y, width, height);
    }
}
