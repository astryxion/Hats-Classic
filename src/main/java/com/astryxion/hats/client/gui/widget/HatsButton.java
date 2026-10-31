package com.astryxion.hats.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * 1.16.5 {@code ElementButton}: minecraft-style BNS chrome with centered text.
 */
public class HatsButton extends Button {

    public HatsButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public static HatsButton create(int x, int y, int width, int height, Component message, OnPress onPress) {
        return new HatsButton(x, y, width, height, message, onPress);
    }

    protected HatsWidgets.ButtonState buttonState() {
        if (!active) {
            return HatsWidgets.ButtonState.CLICK;
        }
        return isHoveredOrFocused() ? HatsWidgets.ButtonState.HOVER : HatsWidgets.ButtonState.IDLE;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        HatsWidgets.renderMinecraftStyleButton(gfx, getX(), getY(), getWidth(), getHeight(), buttonState());
        renderLabel(gfx);
    }

    protected void renderLabel(GuiGraphicsExtractor gfx) {
        if (getMessage().getString().isEmpty()) {
            return;
        }
        var font = Minecraft.getInstance().font;
        int color = active ? 0xFFFFFFFF : 0xFFA0A0A0;
        gfx.centeredText(font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
    }
}
