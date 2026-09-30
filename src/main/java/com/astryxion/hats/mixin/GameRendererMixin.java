package com.astryxion.hats.mixin;

import com.astryxion.hats.client.gui.modern.ModernGuiCamera;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
    private void hats$wideFovForModernGui(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Double> cir) {
        if (ModernGuiCamera.progress(partialTick) > 0f) {
            cir.setReturnValue(100.0);
        }
    }
}
