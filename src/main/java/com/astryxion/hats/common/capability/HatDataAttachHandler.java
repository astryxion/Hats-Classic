package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import net.minecraft.resources.Identifier;

/**
 * Player hat data is provided via HatDataCapability.get(Player) (NeoForge storage).
 * No capability attachment; data is stored in SavedData and synced by packet.
 */
public final class HatDataAttachHandler {

    public static final Identifier HAT_DATA_ID =
            Identifier.fromNamespaceAndPath(Hats.MODID, "hat_data_v2");

    private HatDataAttachHandler() {}
}
