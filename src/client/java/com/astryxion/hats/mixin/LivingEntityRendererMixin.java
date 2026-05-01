package com.astryxion.hats.mixin;

import com.astryxion.hats.client.render.HatRenderStateKeys;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void hats$attachLivingEntityForHatLayer(LivingEntity entity, LivingEntityRenderState renderState, float partialTick, CallbackInfo ci) {
        ((FabricRenderState) (Object) renderState).setData(HatRenderStateKeys.LIVING_ENTITY, entity);
    }
}
