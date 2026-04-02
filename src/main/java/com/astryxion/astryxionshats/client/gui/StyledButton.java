package com.astryxion.astryxionshats.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class StyledButton extends Button {

    public StyledButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int bg = this.isHovered() ? 0xFF9E9E9E : 0xFF7F7F7F;
        gfx.fill(x, y, x + this.width, y + this.height, bg);
        int black = 0xFF000000;
        gfx.fill(x, y, x + this.width, y + 1, black);
        gfx.fill(x, y + this.height - 1, x + this.width, y + this.height, black);
        gfx.fill(x, y, x + 1, y + this.height, black);
        gfx.fill(x + this.width - 1, y, x + this.width, y + this.height, black);

        var font = Minecraft.getInstance().font;
        int textWidth = font.width(this.getMessage());
        int innerW = this.width - 8;
        int textY = y + (this.height - 8) / 2;

        gfx.enableScissor(x + 2, y + 2, x + this.width - 2, y + this.height - 2);

        if (textWidth <= innerW) {
            gfx.drawCenteredString(font, this.getMessage(), x + this.width / 2, textY, 0xFFFFFF);
        } else {
            long t = System.currentTimeMillis() % 4000L;
            int maxOffset = textWidth - innerW;
            int offset = (int) (maxOffset * (double) t / 2000.0);
            if (t > 2000) offset = maxOffset - (int) (maxOffset * (double) (t - 2000) / 2000.0);
            gfx.drawString(font, this.getMessage(), x + 4 - offset, textY, 0xFFFFFF);
        }

        gfx.disableScissor();
    }
}
