package com.astryxion.hats.common.hatstand;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = Hats.MODID)
public final class HatStandEvents {

    private HatStandEvents() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(HatStandRegistry.HAT_STAND_ENTITY.get(), LivingEntity.createLivingAttributes().build());
    }

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(HatStandRegistry.HAT_STAND_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(com.astryxion.hats.common.launcher.HatLauncherRegistry.HAT_LAUNCHER.get());
        }
    }
}
