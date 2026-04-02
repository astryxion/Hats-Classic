package com.astryxion.astryxionshats.common.capability;

import com.astryxion.astryxionshats.AstryxionsHats;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Attaches hat data capability to players
 */
@Mod.EventBusSubscriber(modid = AstryxionsHats.MODID)
public class HatCapabilityEvents {

    private static final ResourceLocation HAT_DATA_ID =
            new ResourceLocation(AstryxionsHats.MODID, "hat_data");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<?> event) {

        if (!(event.getObject() instanceof Player))
            return;

        event.addCapability(HAT_DATA_ID, new HatDataProvider());
    }
}
