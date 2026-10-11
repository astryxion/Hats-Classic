package com.astryxion.hats.common.capability;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;

import net.minecraft.resources.Identifier;

/**
 * The player hat store is the {@code hat_data} attachment registered from {@link HatDataCapability}.
 * This class stays so a second hat-data store is never attached.
 */
@EventBusSubscriber(modid = Hats.MODID)
public class HatCapabilityEvents {

    public static final Identifier HAT_DATA_ID =
            Identifier.fromNamespaceAndPath(Hats.MODID, "hat_data");
}
