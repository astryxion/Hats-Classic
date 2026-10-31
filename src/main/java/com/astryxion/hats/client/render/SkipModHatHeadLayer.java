package com.astryxion.hats.client.render;

import com.astryxion.hats.common.hat.HatManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Hats Renewed never puts mob hats in the HEAD slot, so vanilla never draws a
 * second copy. We still use that slot as a courier, so wrap
 * {@code CustomHeadLayer} and skip our items.
 */
public class SkipModHatHeadLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {

    private final RenderLayer<S, M> vanilla;

    public SkipModHatHeadLayer(RenderLayerParent<S, M> parent, RenderLayer<S, M> vanilla) {
        super(parent);
        this.vanilla = vanilla;
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            S state,
            float yRot,
            float xRot
    ) {
        LivingEntity entity = state.getRenderData(HatRenderLayer.HAT_ENTITY);
        if (entity != null && HatManager.isModHat(entity.getItemBySlot(EquipmentSlot.HEAD))) {
            return;
        }
        vanilla.submit(poseStack, collector, packedLight, state, yRot, xRot);
    }
}
