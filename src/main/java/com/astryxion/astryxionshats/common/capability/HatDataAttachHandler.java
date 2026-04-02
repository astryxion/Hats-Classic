package com.astryxion.astryxionshats.common.capability;

import com.astryxion.astryxionshats.AstryxionsHats;
import net.minecraft.resources.ResourceLocation;

/**
 * Player hat data is provided via HatDataCapability.get(Player) (NeoForge storage).
 * No capability attachment; data is stored in SavedData and synced by packet.
 */
public final class HatDataAttachHandler {

    public static final ResourceLocation HAT_DATA_ID =
            ResourceLocation.fromNamespaceAndPath(AstryxionsHats.MODID, "hat_data_v2");

    private HatDataAttachHandler() {}
}
