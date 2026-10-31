package com.astryxion.hats.mixin;

import com.astryxion.hats.client.gui.modern.ModernGuiCamera;

import net.minecraft.client.Camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class GameRendererMixin {

    @Inject(method = "calculateFov", at = @At("HEAD"), cancellable = true)
    private void hats$wideFovForModernGui(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (ModernGuiCamera.progress(partialTicks) > 0f) {
            cir.setReturnValue(100.0F);
        }
    }
}
