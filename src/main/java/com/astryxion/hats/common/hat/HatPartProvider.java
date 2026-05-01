package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;
import net.minecraft.resources.ResourceLocation;

/**
 * Kept for reference; NeoForge uses HatPartCapability storage instead.
 */
public final class HatPartProvider {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Hats.MODID, "hat_part");

    private HatPartProvider() {}
}
