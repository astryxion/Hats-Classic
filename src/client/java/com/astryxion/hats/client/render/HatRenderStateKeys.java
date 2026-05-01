package com.astryxion.hats.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.world.entity.LivingEntity;

public final class HatRenderStateKeys {
    private HatRenderStateKeys() {}

    public static final RenderStateDataKey<LivingEntity> LIVING_ENTITY =
            RenderStateDataKey.<LivingEntity>create(() -> "hats:hat_layer_living_entity");
}
