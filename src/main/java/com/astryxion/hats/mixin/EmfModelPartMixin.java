package com.astryxion.hats.mixin;

import com.astryxion.hats.client.compat.FreshAnimationsHatCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EMF draws FA bones in {@code renderLikeVanilla} after {@code translateAndRotate}.
 * Snapshot that matrix — it is the pose the skull was actually drawn with.
 */
@Mixin(targets = "traben.entity_model_features.models.parts.EMFModelPart", remap = false)
public abstract class EmfModelPartMixin {

    @Inject(
            method = "renderLikeVanilla",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/ModelPart;m_104299_(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER
            ),
            remap = false,
            require = 0
    )
    private void hats$captureAfterEmfTranslateSrg(
            PoseStack poseStack,
            VertexConsumer vertices,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha,
            CallbackInfo ci
    ) {
        FreshAnimationsHatCompat.captureIfVisualHead((ModelPart) (Object) this, poseStack);
    }

    @Inject(
            method = "renderLikeVanilla",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER
            ),
            remap = false,
            require = 0
    )
    private void hats$captureAfterEmfTranslateOfficial(
            PoseStack poseStack,
            VertexConsumer vertices,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha,
            CallbackInfo ci
    ) {
        FreshAnimationsHatCompat.captureIfVisualHead((ModelPart) (Object) this, poseStack);
    }
}
