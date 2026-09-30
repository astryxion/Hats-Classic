package com.astryxion.hats.mixin;

import com.astryxion.hats.client.compat.FreshAnimationsHatCompat;
import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.render.ClientHatPreview;
import com.astryxion.hats.client.render.HatRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    protected abstract boolean addLayer(RenderLayer<T, M> layer);

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void hats$hideNameForGuiPreview(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (ClientHatPreview.isRendering() || entity == HatsGui.getPreviewEntity()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hats$addHatLayer(CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<T, M> self = (LivingEntityRenderer<T, M>) (Object) this;
        addLayer(new HatRenderLayer<>(self));
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void hats$beginFreshHeadCapture(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        FreshAnimationsHatCompat.beginEntityRender();
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",
                    shift = At.Shift.AFTER
            ),
            require = 0
    )
    private void hats$enterFreshModelAfterAnim(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        FreshAnimationsHatCompat.enterEntityModel(poseStack);
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
                    shift = At.Shift.BEFORE
            ),
            require = 0
    )
    private void hats$enterFreshModel(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        FreshAnimationsHatCompat.enterEntityModel(poseStack);
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
                    shift = At.Shift.AFTER
            ),
            require = 0
    )
    private void hats$leaveFreshModel(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        FreshAnimationsHatCompat.leaveEntityModel();
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void hats$endFreshHeadCapture(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        FreshAnimationsHatCompat.endEntityRender();
    }
}
