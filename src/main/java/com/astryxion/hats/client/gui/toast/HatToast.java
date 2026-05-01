package com.astryxion.hats.client.gui.toast;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class HatToast implements Toast {

    private static final int TOAST_WIDTH = 160;
    private static final int TOAST_HEIGHT = 32;
    private static final ResourceLocation TOAST_BACKGROUND_SPRITE = ResourceLocation.fromNamespaceAndPath("minecraft", "toast/recipe");
    /** Same purple as vanilla "New Recipes Unlocked!" title (recipe toast) */
    private static final int TITLE_COLOR = 0xFF55FF;

    private final ItemStack hatStack;

    public HatToast(ItemStack stack) {
        this.hatStack = stack;
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
    public Visibility render(
            GuiGraphics gfx,
            ToastComponent toasts,
            long time
    ) {
        gfx.blitSprite(TOAST_BACKGROUND_SPRITE, 0, 0, TOAST_WIDTH, TOAST_HEIGHT);

        gfx.drawString(
                toasts.getMinecraft().font,
                Component.literal("Hat Unlocked!").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TITLE_COLOR))),
                30,
                7,
                0xFFFFFF,
                false
        );

        gfx.drawString(
                toasts.getMinecraft().font,
                hatStack.getHoverName().copy().withStyle(ChatFormatting.BLACK),
                30,
                18,
                0xFFFFFF,
                false
        );

        gfx.renderFakeItem(hatStack, 8, 8);

        return time >= 5000
                ? Visibility.HIDE
                : Visibility.SHOW;
    }
}
