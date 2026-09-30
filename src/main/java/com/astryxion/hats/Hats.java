package com.astryxion.hats;

import com.astryxion.hats.common.hat.HatRarityLoader;
import com.astryxion.hats.common.hatstand.HatStandRegistry;
import com.astryxion.hats.common.launcher.HatLauncherRegistry;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.registry.HatItemRegistry;
import com.astryxion.hats.common.sound.HatsSounds;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

/**
 * Main entry for the Hats mod.
 */
@Mod(Hats.MODID)
public class Hats {

    public static final String MODID = "hats";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Hats() {

        IEventBus modEventBus =
                FMLJavaModLoadingContext.get().getModEventBus();

        // =========================
        // CONFIG (THIS FIXES SPAWNING)
        // =========================

        ModLoadingContext.get().registerConfig(
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

        // =========================
        // Lifecycle
        // =========================

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Hats mod initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        // IMPORTANT: register packets after everything loads
        event.enqueueWork(() -> {
            HatRarityLoader.load();
            HatPacketHandler.register();
        });

        LOGGER.info("Hats mod common setup complete.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        LOGGER.info("Hats mod server starting.");
    }
}
