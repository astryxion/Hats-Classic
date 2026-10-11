package com.astryxion.hats.mixin;

import com.astryxion.hats.client.render.HatRenderLayer;
import com.astryxion.hats.common.hat.HatManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Villagers and illagers keep the hat in the HEAD slot as a courier.
 * Vanilla {@link CustomHeadLayer} also draws that slot, so those mobs rendered
 * two hats. Skip the vanilla layer for our items; {@code HatRenderLayer} owns
 * placement.
 */
@Mixin(CustomHeadLayer.class)
public class CustomHeadLayerMixin {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void hats$skipModHats(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            LivingEntityRenderState state,
            float yRot,
            float xRot,
            CallbackInfo ci
    ) {
        LivingEntity entity = state.getRenderData(HatRenderLayer.HAT_ENTITY);
        if (entity != null && HatManager.isModHat(entity.getItemBySlot(EquipmentSlot.HEAD))) {
            ci.cancel();
        }
    }
}
