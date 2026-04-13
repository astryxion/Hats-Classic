package com.astryxion.hats.client.render;

import com.astryxion.hats.Hats;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;

public final class HatRenderStateKeys {
    private HatRenderStateKeys() {}

    public static final ContextKey<LivingEntity> LIVING_ENTITY =
            new ContextKey<>(Identifier.fromNamespaceAndPath(Hats.MODID, "hat_layer_living_entity"));
}
