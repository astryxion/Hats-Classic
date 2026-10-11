package com.astryxion.hats.mixin;

import com.astryxion.hats.client.render.EnderDragonHatRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Capture the collector and dragon for {@link EnderDragonModelMixin}. The mesh
 * is drawn later, so the collector stays until both model passes flush the hat.
 */
@Mixin(EnderDragonRenderer.class)
public abstract class EnderDragonRendererMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void hats$dragon(EnderDragon dragon, EnderDragonRenderState state, float partialTicks, CallbackInfo ci) {
        EnderDragonHatRenderer.bindDragon(dragon);
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private void hats$capture(
            EnderDragonRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        EnderDragonHatRenderer.beginFrame(collector, state.lightCoords);
    }
}
