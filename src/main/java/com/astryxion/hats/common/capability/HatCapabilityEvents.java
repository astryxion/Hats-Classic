package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;

import net.minecraft.resources.ResourceLocation;

/**
 * The player hat store is the {@code hat_data} attachment registered from {@link HatDataCapability}.
 * This class stays so a second hat-data store is never attached.
 */
public class HatCapabilityEvents {

    public static final ResourceLocation HAT_DATA_ID =
            ResourceLocation.fromNamespaceAndPath(Hats.MODID, "hat_data");
}
