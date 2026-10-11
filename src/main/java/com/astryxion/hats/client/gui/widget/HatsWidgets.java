package com.astryxion.hats.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * 1.16.5 iChunUtil BNS minecraft-style button drawing.
 * 26.1 replaced {@code textures/gui/widgets.png} with nine-slice widget sprites.
 * Idle is the normal button, hover is the highlighted button, and click is the
 * disabled button, matching the old rows at y=66, y=86, and y=46.
 */
public final class HatsWidgets {

    private static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");
    private static final Identifier BUTTON_DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");

    public enum ButtonState {
        IDLE,
        HOVER,
        CLICK
    }

    private HatsWidgets() {}

    public static void renderMinecraftStyleButton(GuiGraphicsExtractor gfx, int x, int y, int width, int height, ButtonState state) {
        Identifier sprite = switch (state) {
            case HOVER -> BUTTON_HIGHLIGHTED;
            case CLICK -> BUTTON_DISABLED;
            case IDLE -> BUTTON;
        };
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }
}
