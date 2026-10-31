package com.astryxion.hats.client.tabula;

import com.astryxion.hats.common.tabula.TabulaHats;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a Tabula hat in a GUI slot the same way Hats Renewed draws toast hats:
 * no Y-flip. {@code renderFakeItem} cannot show these because they are entity
 * models, not 2D item icons.
 */
public final class TabulaHatGuiPreview {

    private TabulaHatGuiPreview() {}

    public static void render(GuiGraphics graphics, ItemStack stack, int x, int y, int size) {
        if (stack == null || stack.isEmpty() || !TabulaHats.isTabula(stack)) {
            return;
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + size / 2.0F, y + size / 2.0F, 100.0F);
        pose.mulPose(Axis.YP.rotationDegrees(205.0F));
        float scale = size * 0.7F;
        pose.scale(scale, scale, scale);

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        graphics.flush();
        RenderSystem.enableDepthTest();
        TabulaHatRenderer.render(pose, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, stack, 1.0F);
        buffers.endBatch();
        RenderSystem.disableDepthTest();
        pose.popPose();
    }
}
