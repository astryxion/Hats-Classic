package com.astryxion.hats.mixin;

import net.minecraft.client.model.BabyModelTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BabyModelTransform.class)
public interface AgeableHierarchicalModelAccessor {

    @Accessor("bodyYOffset")
    float hats$getBodyYOffset();
}
