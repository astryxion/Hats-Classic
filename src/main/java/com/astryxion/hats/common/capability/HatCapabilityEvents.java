package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Attaches hat data capability to players
 */
@Mod.EventBusSubscriber(modid = Hats.MODID)
public class HatCapabilityEvents {

    private static final ResourceLocation HAT_DATA_ID =
            new ResourceLocation(Hats.MODID, "hat_data");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<?> event) {

        if (!(event.getObject() instanceof Player))
            return;

        event.addCapability(HAT_DATA_ID, new HatDataProvider());
    }
}
