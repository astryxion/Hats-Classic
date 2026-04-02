package com.astryxion.astryxionshats.client.gui.toast;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class HatToast implements Toast {

    private final ItemStack hatStack;

    public HatToast(ItemStack stack) {
        this.hatStack = stack;
    }

    @Override
    public Visibility render(
            GuiGraphics gfx,
            ToastComponent toasts,
            long time
    ) {
        gfx.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());

        gfx.drawString(
                toasts.getMinecraft().font,
                Component.literal("Hat Unlocked!"),
                30,
                7,
                0xFFFFAA,
                false
        );

        gfx.drawString(
                toasts.getMinecraft().font,
                hatStack.getHoverName(),
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
