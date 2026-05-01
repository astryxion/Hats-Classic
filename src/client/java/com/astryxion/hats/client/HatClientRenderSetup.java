package com.astryxion.hats.client;

import com.astryxion.hats.client.render.HatRenderLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class HatClientRenderSetup {
    private HatClientRenderSetup() {}

    public static void register() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof LivingEntityRenderer<?, ?, ?> ler) {
                registrationHelper.register(new HatRenderLayer((LivingEntityRenderer<?, LivingEntityRenderState, ?>) (Object) ler));
            }
        });
    }
}
