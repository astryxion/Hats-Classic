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

    private static final long DISPLAY_TIME_MS = 5000L;
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("toast/advancement");

    private final ItemStack hatStack;
    private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;

    public HatToast(ItemStack stack) {
        this.hatStack = stack;
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        wantedVisibility = fullyVisibleForMs >= DISPLAY_TIME_MS ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, Font font, long fullyVisibleForMs) {
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, this.width(), this.height());

        gfx.text(
                font,
                Component.literal("Hat Unlocked!"),
                30,
                7,
                0xFFFFFFAA,
                false
        );

        gfx.text(
                font,
                hatStack.getHoverName(),
                30,
                18,
                0xFFFFFFFF,
                false
        );

        if (com.astryxion.hats.common.tabula.TabulaHats.isTabula(hatStack)) {
            com.astryxion.hats.client.tabula.TabulaHatGuiPreview.render(gfx, hatStack, 6, 6, 20);
        } else {
            gfx.fakeItem(hatStack, 8, 8);
        }
    }
}
