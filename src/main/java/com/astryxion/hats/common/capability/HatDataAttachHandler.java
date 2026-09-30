package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Used to attach a second {@code hat_data_v2} provider. That second copy is
 * why trades never saved. Left in place so the event class stays registered
 * but it must not attach another {@code HAT_DATA}.
 */
@Mod.EventBusSubscriber(modid = Hats.MODID)
public class HatDataAttachHandler {

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
    }
}
