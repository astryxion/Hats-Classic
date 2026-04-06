package com.astryxion.hats.mixin;

import com.astryxion.hats.client.render.HatRenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hats$addHatLayer(CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> self = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) (Object) this;
        RenderLayer<LivingEntity, EntityModel<LivingEntity>> layer = new HatRenderLayer<>(self);
        try {
            Class<?> cl = self.getClass();
            while (cl != null && cl != Object.class) {
                for (Method m : cl.getDeclaredMethods()) {
                    if (m.getParameterCount() == 1
                            && RenderLayer.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        m.setAccessible(true);
                        m.invoke(self, layer);
                        return;
                    }
                }
                cl = cl.getSuperclass();
            }
        } catch (Exception ignored) {
        }
    }
}
