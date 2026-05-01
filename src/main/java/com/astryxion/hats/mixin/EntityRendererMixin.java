package com.astryxion.hats.mixin;

import com.astryxion.hats.client.gui.HatScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
    private void hats$hideNameForPreview(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity == HatScreen.getPreviewEntityForRendering()) {
            cir.setReturnValue(false);
        }
    }
}
