package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import net.minecraft.resources.Identifier;

/**
 * Hat data is provided via HatDataCapability.get(Player) (NeoForge).
 */
public final class HatCapabilityEvents {

    private static final Identifier HAT_DATA_ID =
            Identifier.fromNamespaceAndPath(Hats.MODID, "hat_data");

    private HatCapabilityEvents() {}
}
