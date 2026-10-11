package com.astryxion.hats.mixin;

import com.astryxion.hats.client.render.EnderDragonHatRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bind the head for matrix capture, then flush the hat after the mesh pass so
 * item rendering cannot clobber the dragon's VertexConsumer / texture.
 * {@code renderToBuffer} is final on {@link Model}, so the inject lives there
 * and only runs for the dragon model.
 */
@Mixin(Model.class)
public abstract class EnderDragonModelMixin {

    @Inject(
            method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At("HEAD")
    )
    private void hats$bindHead(CallbackInfo ci) {
        if (!(((Object) this) instanceof EnderDragonModel)) {
            return;
        }
        EnderDragon dragon = EnderDragonHatRenderer.dragon();
        ModelPart root = ((Model<?>) (Object) this).root();
        if (dragon != null && root != null && root.hasChild("head")) {
            EnderDragonHatRenderer.bindHead(dragon, root.getChild("head"));
        }
    }

    @Inject(
            method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At("RETURN")
    )
    private void hats$flushHat(
            PoseStack poseStack,
            VertexConsumer consumer,
            int light,
            int overlay,
            int color,
            CallbackInfo ci
    ) {
        if (!(((Object) this) instanceof EnderDragonModel)) {
            return;
        }
        EnderDragonHatRenderer.flushAfterModel(poseStack);
        EnderDragonHatRenderer.unbindHead();
        EnderDragonHatRenderer.finishPass();
    }
}
