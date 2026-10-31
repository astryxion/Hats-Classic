package com.astryxion.hats.mixin;

import com.astryxion.hats.common.hat.HatManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
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
public class CustomHeadLayerMixin<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hats$skipModHats(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (HatManager.isModHat(entity.getItemBySlot(EquipmentSlot.HEAD))) {
            ci.cancel();
        }
    }
}
