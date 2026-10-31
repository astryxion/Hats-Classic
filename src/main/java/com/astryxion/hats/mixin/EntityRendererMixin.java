package com.astryxion.hats.mixin;

import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.render.ClientHatPreview;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z", at = @At("HEAD"), cancellable = true)
    private void hats$hideNameForPreview(T entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
        if (ClientHatPreview.isRendering() || entity == HatsGui.getPreviewEntity()) {
            cir.setReturnValue(false);
        }
    }
}
