package com.astryxion.hats;

import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.hat.HatRarityLoader;
import com.astryxion.hats.common.hatstand.HatStandRegistry;
import com.astryxion.hats.common.launcher.HatLauncherRegistry;
import com.astryxion.hats.common.registry.HatItemRegistry;
import com.astryxion.hats.common.sound.HatsSounds;

import com.mojang.logging.LogUtils;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import org.slf4j.Logger;

/**
 * Main entry for the Hats mod.
 */
@Mod(Hats.MODID)
public class Hats {

    public static final String MODID = "hats";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Hats(IEventBus modEventBus, ModContainer modContainer) {

        // =========================
        // CONFIG (THIS FIXES SPAWNING)
        // =========================

        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                Config.SPEC
        );

        // =========================
        // Registries
        // =========================

        HatItemRegistry.register(modEventBus);
        HatStandRegistry.register(modEventBus);
        HatLauncherRegistry.register(modEventBus);
        HatsSounds.register(modEventBus);
        HatDataCapability.register(modEventBus);
        HatPartCapability.register(modEventBus);

        // =========================
        // Lifecycle
        // =========================

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("Hats mod initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {
            HatRarityLoader.load();
        });

        LOGGER.info("Hats mod common setup complete.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        LOGGER.info("Hats mod server starting.");
    }
}
