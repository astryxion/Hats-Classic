package com.astryxion.hats.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * 1.16.5 {@code ElementButtonTextured}: BNS button with a centered icon.
 */
public class HatsIconButton extends HatsButton {

    private final Identifier icon;

    public HatsIconButton(int x, int y, int width, int height, Identifier icon, Component tooltip, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress);
        this.icon = icon;
        if (tooltip != null) {
            setTooltip(Tooltip.create(tooltip));
        }
    }

    @Override
    protected void renderLabel(GuiGraphicsExtractor gfx) {
        int length = Math.min(getWidth(), getHeight()) - 4;
        int iconX = getX() + (getWidth() - length) / 2;
        int iconY = getY() + (getHeight() - length) / 2;
        gfx.blit(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, 0.0F, 0.0F, length, length, 16, 16, 16, 16);
    }
}
