package com.astryxion.hats.common.hatstand;

import com.astryxion.hats.Hats;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hats.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
