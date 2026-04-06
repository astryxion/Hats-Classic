package com.astryxion.hats.common.hat;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Holds the HatPart capability reference ONLY
 * (No syncing logic here – handled by ServerHatHandler)
 */
public class HatPartCapability {

    public static final Capability<HatPart> HAT_PART =
            CapabilityManager.get(new CapabilityToken<>() {});
}
