package com.astryxion.hats.client.tabula;

import com.astryxion.hats.common.tabula.TabulaHats;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TabulaHatItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static TabulaHatItemRenderer instance;

    public static TabulaHatItemRenderer getInstance() {
        if (instance == null) {
            Minecraft mc = Minecraft.getInstance();
            instance = new TabulaHatItemRenderer(mc);
        }
        return instance;
    }

    private TabulaHatItemRenderer(Minecraft mc) {
        super(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        if (!TabulaHats.isTabula(stack)) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.translate(0.0F, -8.0F / 16.0F, 0.0F);
        TabulaHatPlacement.applyZFight(poseStack);
        TabulaHatRenderer.render(poseStack, buffers, light, overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay, stack, 1.0F);
        poseStack.popPose();
    }
}
