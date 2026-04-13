package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import net.minecraft.resources.ResourceLocation;

/**
 * Player hat data is provided via HatDataCapability.get(Player) (NeoForge storage).
 * No capability attachment; data is stored in SavedData and synced by packet.
 */
public final class HatDataAttachHandler {

    public static final ResourceLocation HAT_DATA_ID =
            ResourceLocation.fromNamespaceAndPath(Hats.MODID, "hat_data_v2");

    private HatDataAttachHandler() {}
}
