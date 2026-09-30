package com.astryxion.hats.client.render;

import com.astryxion.hats.common.launcher.HatEntity;
import com.astryxion.hats.common.trade.TradeHats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HatEntityRenderer extends EntityRenderer<HatEntity> {

    public HatEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            HatEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light
    ) {
        Item item = TradeHats.item(entity.getHatId());
        if (item == null) {
            return;
        }
        float rotY = Mth.lerp(partialTick, entity.lastRotY, entity.rotY);
        float rotX = Mth.lerp(partialTick, entity.lastRotX, entity.rotX);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotX));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                new ItemStack(item),
                ItemDisplayContext.HEAD,
                light,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffers,
                entity.level(),
                entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(HatEntity entity) {
        return new ResourceLocation("hats", "textures/model/hat_launcher.png");
    }
}
