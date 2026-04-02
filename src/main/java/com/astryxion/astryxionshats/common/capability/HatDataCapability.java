package com.astryxion.astryxionshats.common.capability;

import com.astryxion.astryxionshats.common.hat.PlayerHatData;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Capability definition for player hat data
 */
public class HatDataCapability {

    public static final Capability<PlayerHatData> HAT_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});
}
