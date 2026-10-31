package com.astryxion.hats.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Marquee for hat names that do not fit, matching the legacy chooser / Hats Renewed trade UI.
 */
public final class HatNameScroller {

    private HatNameScroller() {}

    public static void draw(GuiGraphics graphics, Font font, String name, int x, int y, int width, int color) {
        draw(graphics, font, name, x, y, width, color, 0.7f);
    }

    public static void draw(
            GuiGraphics graphics,
            Font font,
            String name,
            int x,
            int y,
            int width,
            int color,
            float scale
    ) {
        if (name == null || name.isEmpty() || width <= 0) {
            return;
        }
        int textWidth = (int) (font.width(name) * scale);
        int xOff;
        if (textWidth <= width) {
            xOff = (width - textWidth) / 2;
        } else {
            float overflow = textWidth - width;
            float scrollMs = overflow * 50f;
            float pauseMs = 1000f;
            long period = Math.max(100L, (long) (2 * (scrollMs + pauseMs)));
            long time = System.currentTimeMillis() % period;
            float progress;
            if (time < pauseMs) {
                progress = 0f;
            } else if (time < pauseMs + scrollMs) {
                progress = (time - pauseMs) / scrollMs;
            } else if (time < 2 * pauseMs + scrollMs) {
                progress = 1f;
            } else {
                progress = 1f - (time - 2 * pauseMs - scrollMs) / scrollMs;
            }
            xOff = -(int) (overflow * progress);
        }

        int clipBottom = y + Math.max(1, (int) (font.lineHeight * scale) + 1);
        graphics.enableScissor(x, y - 1, x + width, clipBottom);
        graphics.pose().pushPose();
        graphics.pose().translate(x + xOff, y, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, name, 0, 0, color, false);
        graphics.pose().popPose();
        graphics.disableScissor();
    }
}
