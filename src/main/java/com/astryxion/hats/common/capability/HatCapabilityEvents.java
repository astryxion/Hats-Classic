package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import net.minecraft.resources.ResourceLocation;

/**
 * Hat data is provided via HatDataCapability.get(Player) (NeoForge).
 */
public final class HatCapabilityEvents {

    private static final ResourceLocation HAT_DATA_ID =
            ResourceLocation.fromNamespaceAndPath(Hats.MODID, "hat_data");

    private HatCapabilityEvents() {}
}
