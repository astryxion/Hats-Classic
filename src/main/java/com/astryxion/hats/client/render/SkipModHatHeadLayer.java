package com.astryxion.hats.client.render;

import com.astryxion.hats.common.hat.HatManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Hats Renewed never puts mob hats in the HEAD slot, so vanilla never draws a
 * second copy. We still use that slot as a courier, so wrap
 * {@code CustomHeadLayer} and skip our items.
 */
public class SkipModHatHeadLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private final RenderLayer<T, M> vanilla;

    public SkipModHatHeadLayer(RenderLayerParent<T, M> parent, RenderLayer<T, M> vanilla) {
        super(parent);
        this.vanilla = vanilla;
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (HatManager.isModHat(entity.getItemBySlot(EquipmentSlot.HEAD))) {
            return;
        }
        vanilla.render(
                poseStack,
                buffer,
                packedLight,
                entity,
                limbSwing,
                limbSwingAmount,
                partialTicks,
                ageInTicks,
                netHeadYaw,
                headPitch
        );
    }
}
