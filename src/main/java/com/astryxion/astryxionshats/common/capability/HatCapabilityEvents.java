package com.astryxion.astryxionshats.common.capability;

import com.astryxion.astryxionshats.AstryxionsHats;
import net.minecraft.resources.ResourceLocation;

/**
 * Hat data is provided via HatDataCapability.get(Player) (Fabric).
 */
public final class HatCapabilityEvents {

    private static final ResourceLocation HAT_DATA_ID =
            ResourceLocation.fromNamespaceAndPath(AstryxionsHats.MODID, "hat_data");

    private HatCapabilityEvents() {}
}
