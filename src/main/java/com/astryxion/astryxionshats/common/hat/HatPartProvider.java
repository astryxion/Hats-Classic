package com.astryxion.astryxionshats.common.hat;

import com.astryxion.astryxionshats.AstryxionsHats;
import net.minecraft.resources.ResourceLocation;

/**
 * Kept for reference; Fabric uses HatPartCapability storage instead.
 */
public final class HatPartProvider {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AstryxionsHats.MODID, "hat_part");

    private HatPartProvider() {}
}
