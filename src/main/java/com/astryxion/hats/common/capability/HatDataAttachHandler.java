package com.astryxion.hats.common.capability;

import net.minecraft.world.entity.Entity;

/**
 * Used to attach a second {@code hat_data_v2} provider. That second copy is
 * why trades never saved. Left in place so it must not attach another {@code HAT_DATA}.
 */
public class HatDataAttachHandler {

    public static void attachCapabilities(Entity entity) {
    }
}
