package com.astryxion.hats.mixin;

import com.astryxion.hats.client.render.EnderDragonHatRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bind the head for matrix capture, then flush the hat after the mesh pass so
 * item rendering cannot clobber the dragon's VertexConsumer / texture.
 */
@Mixin(EnderDragonRenderer.DragonModel.class)
public abstract class EnderDragonModelMixin {

    @Shadow
    private ModelPart head;

    @Shadow
    private EnderDragon entity;

    @Inject(
            method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At("HEAD")
    )
    private void hats$bindHead(CallbackInfo ci) {
        if (this.entity != null && this.head != null) {
            EnderDragonHatRenderer.bindHead(this.entity, this.head);
        }
    }

    @Inject(
            method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At("RETURN")
    )
    private void hats$flushHat(
            PoseStack poseStack,
            VertexConsumer consumer,
            int light,
            int overlay,
            float r,
            float g,
            float b,
            float a,
            CallbackInfo ci
    ) {
        EnderDragonHatRenderer.flushAfterModel(poseStack);
        EnderDragonHatRenderer.unbindHead();
    }
}
