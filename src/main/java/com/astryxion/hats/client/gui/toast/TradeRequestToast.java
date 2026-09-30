package com.astryxion.hats.client.gui.toast;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;

public class TradeRequestToast implements Toast {

    private static final long DISPLAY_TIME_MS = 7000L;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 32;

    private final String senderName;
    private long startTime = -1;

    public TradeRequestToast(String senderName) {
        this.senderName = senderName;
    }

    @Override
    public int width() {
        return WIDTH;
    }

    @Override
    public int height() {
        return HEIGHT;
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toasts, long timeSinceLastVisible) {
        if (startTime < 0) {
            startTime = timeSinceLastVisible;
        }
        long elapsed = timeSinceLastVisible - startTime;
        graphics.fill(0, 0, WIDTH, HEIGHT, 0xFF202020);
        graphics.fill(1, 1, WIDTH - 1, HEIGHT - 1, 0xFF2B2B2B);
        Minecraft minecraft = toasts.getMinecraft();
        graphics.drawString(minecraft.font,
                Component.translatable("hats.trade.requestBy").withStyle(ChatFormatting.GOLD),
                6, 6, 0xFFAA00, false);
        graphics.drawString(minecraft.font,
                Component.literal(senderName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                6, 18, 0xFF55FF55, false);
        int barY = HEIGHT - 3;
        int barX = 4;
        int barW = WIDTH - 8;
        graphics.fill(barX, barY, barX + barW, barY + 2, 0xFF444444);
        float fraction = 1f - Math.min(1f, (float) elapsed / DISPLAY_TIME_MS);
        int filled = (int) (barW * fraction);
        if (filled > 0) {
            graphics.fill(barX, barY, barX + filled, barY + 2, 0xFFFFAA00);
        }
        return elapsed >= DISPLAY_TIME_MS ? Visibility.HIDE : Visibility.SHOW;
    }

    public static void show(String senderName) {
        Minecraft.getInstance().getToasts().addToast(new TradeRequestToast(senderName));
    }
}
