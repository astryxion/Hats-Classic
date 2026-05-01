package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;
import net.minecraft.resources.Identifier;

/**
 * Kept for reference; NeoForge uses HatPartCapability storage instead.
 */
public final class HatPartProvider {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(Hats.MODID, "hat_part");

    private HatPartProvider() {}
}
