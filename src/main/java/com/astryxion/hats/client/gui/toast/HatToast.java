package com.astryxion.hats.client.gui.toast;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class HatToast implements Toast {

    private static final int TOAST_WIDTH = 160;
    private static final int TOAST_HEIGHT = 32;
    private static final Identifier TOAST_BACKGROUND_SPRITE = Identifier.fromNamespaceAndPath("minecraft", "toast/recipe");
    /** Full ARGB for toast title (bright magenta). */
    private static final int TITLE_COLOR_ARGB = 0xFFFF55FF;
    /** Opaque black for hat name — avoids fuzzy blended black from styled text + color -1. */
    private static final int SUBTITLE_COLOR_ARGB = 0xFF000000;

    private final ItemStack hatStack;
    private long displayTime;

    public HatToast(ItemStack stack) {
        this.hatStack = stack.copy();
    }

    @Override
    public int width() {
        return TOAST_WIDTH;
    }

    @Override
    public int height() {
        return TOAST_HEIGHT;
    }

    @Override
    public void update(ToastManager manager, long time) {
        this.displayTime = time;
    }

    @Override
    public Visibility getWantedVisibility() {
        return displayTime >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long time) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TOAST_BACKGROUND_SPRITE, 0, 0, TOAST_WIDTH, TOAST_HEIGHT);

        graphics.text(font, Component.literal("Hat Unlocked!"), 30, 7, TITLE_COLOR_ARGB);

        graphics.text(font, hatStack.getHoverName(), 30, 18, SUBTITLE_COLOR_ARGB);

        graphics.item(hatStack, 8, 8);
    }
}
