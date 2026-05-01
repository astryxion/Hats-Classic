package com.astryxion.hats.common.capability;

import com.astryxion.hats.AstryxionsHats;
import net.minecraft.resources.ResourceLocation;

/**
 * Hat data is provided via HatDataCapability.get(Player) (Fabric).
 */
public final class HatCapabilityEvents {

    private static final ResourceLocation HAT_DATA_ID =
            new ResourceLocation(AstryxionsHats.MODID, "hat_data");

    private HatCapabilityEvents() {}
}
