package com.astryxion.hats.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.16.5 iChunUtil BNS minecraft-style button drawing, ported to 1.20.1.
 */
public final class HatsWidgets {

    public static final ResourceLocation VANILLA_WIDGETS = new ResourceLocation("textures/gui/widgets.png");

    public enum ButtonState {
        IDLE,
        HOVER,
        CLICK
    }

    private HatsWidgets() {}

    public static void renderMinecraftStyleButton(GuiGraphics gfx, int x, int y, int width, int height, ButtonState state) {
        int yOffset = state == ButtonState.CLICK ? 0 : state == ButtonState.HOVER ? 2 : 1;
        if (height == 20 && width > 15) {
            int remaining = width - 28;
            int cursor = x + 14;
            while (remaining > 0) {
                int dist = Math.min(remaining, 172);
                blit(gfx, cursor, y, dist, 20, 14, 46 + yOffset * 20, dist, 20);
                remaining -= dist;
                cursor += dist;
            }
            blit(gfx, x, y, 14, 20, 0, 46 + yOffset * 20, 14, 20);
            blit(gfx, x + width - 14, y, 14, 20, 186, 46 + yOffset * 20, 14, 20);
            return;
        }
        if (height < 20 && width < 200) {
            int slice = height - 3;
            int wide = width - 3;
            blit(gfx, x, y + height - slice, wide, slice, 0, 66 - slice + yOffset * 20, wide, slice);
            blit(gfx, x, y, wide, slice, 0, 46 + yOffset * 20, wide, slice);
            blit(gfx, x + width - wide, y, wide, slice, 200 - wide, 46 + yOffset * 20, wide, slice);
            blit(gfx, x + width - wide, y + height - slice, wide, slice, 200 - wide, 66 - slice + yOffset * 20, wide, slice);
            return;
        }
        cropAndStitch(gfx, x, y, width, height, 4, 0, 46 + yOffset * 20, 200, 20);
    }

    private static void cropAndStitch(GuiGraphics gfx, int x, int y, int width, int height, int border, int u, int v, int uLength, int vLength) {
        int innerW = width - border * 2;
        int innerX = x + border;
        while (innerW > 0) {
            int innerH = height - border * 2;
            int innerY = y + border;
            int distX = Math.min(innerW, uLength - border * 2);
            while (innerH > 0) {
                int distY = Math.min(innerH, vLength - border * 2);
                blit(gfx, innerX, innerY, distX, distY, u + border, v + border, distX, distY);
                innerH -= distY;
                innerY += distY;
            }
            innerW -= distX;
            innerX += distX;
        }

        int bar = width - border * 2;
        int barX = x + border;
        while (bar > 0) {
            int dist = Math.min(bar, uLength - border * 2);
            blit(gfx, barX, y, dist, border, u + border, v, dist, border);
            blit(gfx, barX, y + height - border, dist, border, u + border, v + vLength - border, dist, border);
            bar -= dist;
            barX += dist;
        }

        int side = height - border * 2;
        int sideY = y + border;
        while (side > 0) {
            int dist = Math.min(side, vLength - border * 2);
            blit(gfx, x, sideY, border, dist, u, v + border, border, dist);
            blit(gfx, x + width - border, sideY, border, dist, u + uLength - border, v + border, border, dist);
            side -= dist;
            sideY += dist;
        }

        blit(gfx, x, y + height - border, border, border, u, v + vLength - border, border, border);
        blit(gfx, x, y, border, border, u, v, border, border);
        blit(gfx, x + width - border, y, border, border, u + uLength - border, v, border, border);
        blit(gfx, x + width - border, y + height - border, border, border, u + uLength - border, v + vLength - border, border, border);
    }

    private static void blit(GuiGraphics gfx, int x, int y, int w, int h, int u, int v, int uW, int vH) {
        gfx.blit(VANILLA_WIDGETS, x, y, w, h, u, v, Math.max(1, uW), Math.max(1, vH), 256, 256);
    }
}
