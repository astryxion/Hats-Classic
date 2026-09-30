package com.astryxion.hats.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.16.5 {@code ElementButtonTextured}: BNS button with a centered icon.
 */
public class HatsIconButton extends HatsButton {

    private final ResourceLocation icon;

    public HatsIconButton(int x, int y, int width, int height, ResourceLocation icon, Component tooltip, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress);
        this.icon = icon;
        if (tooltip != null) {
            setTooltip(Tooltip.create(tooltip));
        }
    }

    @Override
    protected void renderLabel(GuiGraphics gfx) {
        int length = Math.min(getWidth(), getHeight()) - 4;
        int iconX = getX() + (getWidth() - length) / 2;
        int iconY = getY() + (getHeight() - length) / 2;
        gfx.blit(icon, iconX, iconY, length, length, 0, 0, 16, 16, 16, 16);
    }
}
