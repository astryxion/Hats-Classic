package com.astryxion.hats.common.capability;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;

import net.minecraft.world.entity.Entity;

/**
 * Used to attach a second {@code hat_data_v2} provider. That second copy is
 * why trades never saved. Left in place so the event class stays registered
 * but it must not attach another {@code HAT_DATA}.
 */
@EventBusSubscriber(modid = Hats.MODID)
public class HatDataAttachHandler {

    public static void attachCapabilities(Entity entity) {
    }
}
