package com.astryxion.hats.mixin;

import com.astryxion.hats.client.compat.FreshAnimationsHatCompat;
import com.astryxion.hats.client.render.EnderDragonHatRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {

    @Inject(method = "translateAndRotate", at = @At("TAIL"))
    private void hats$captureFreshHead(PoseStack poseStack, CallbackInfo ci) {
        ModelPart self = (ModelPart) (Object) this;
        FreshAnimationsHatCompat.captureIfVisualHead(self, poseStack);
        EnderDragonHatRenderer.onHeadTranslated(self, poseStack);
    }
}
