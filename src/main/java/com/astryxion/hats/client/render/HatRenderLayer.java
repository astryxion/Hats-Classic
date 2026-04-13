package com.astryxion.hats.client.render;

import com.astryxion.hats.client.compat.MoBendsHatCompat;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.client.render.helper.HatRendererHelper;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;

public class HatRenderLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    public HatRenderLayer(LivingEntityRenderer<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {

        ItemStack hat = HatManager.getHat(entity);

        if (hat.isEmpty())
            return;

        poseStack.pushPose();

        EntityModel<T> model = getParentModel();

        boolean applied = false;

        // Mo' Bends: BendsModelPart rigs; vanilla ModelPart sync is incomplete for layers (see MoBendsHatCompat).
        if (MoBendsHatCompat.isMoBendsLoaded() && MoBendsHatCompat.tryApplyAnimatedMobendsHead(poseStack)) {
            applied = true;
        }

        // ============================
        // MODERN DEV PATH
        // ============================

        if (!applied && model instanceof HeadedModel headed) {
            headed.getHead().translateAndRotate(poseStack);
            applied = true;
        }

        // ============================
        // PRODUCTION SAFE FALLBACK
        // ============================

        if (!applied) {

            ModelPart head = findHeadPart(model);

            if (head != null) {
                head.translateAndRotate(poseStack);
            }
        }

        // ============================
        // Your helper offsets
        // ============================

        HatRendererHelper.applyTransforms(
                entity,
                poseStack,
                partialTicks
        );

        // ============================
        // Render hat item
        // ============================

        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity,
                hat,
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                buffer,
                entity.level(),
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }

    // ============================
    // Heuristic animated head finder
    // (works dev + production)
    // ============================

    private ModelPart findHeadPart(Object model) {

        // Try HeadedModel again (safety)
        if (model instanceof HeadedModel headed) {
            return headed.getHead();
        }

        Class<?> search = model.getClass();

        while (search != null && search != Object.class) {

            for (Field field : search.getDeclaredFields()) {

                try {
                    field.setAccessible(true);
                    Object value = field.get(model);

                    if (value instanceof ModelPart part) {

                        String name = field.getName().toLowerCase();

                        // Almost all head bones contain "head"
                        // obf ones start with f_
                        if (name.contains("head") || name.startsWith("f_")) {
                            return part;
                        }
                    }

                } catch (Exception ignored) {}
            }

            search = search.getSuperclass();
        }

        return null;
    }
}
