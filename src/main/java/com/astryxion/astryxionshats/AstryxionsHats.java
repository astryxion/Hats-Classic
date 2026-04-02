package com.astryxion.astryxionshats;

import com.astryxion.astryxionshats.common.network.HatPacketHandler;
import com.astryxion.astryxionshats.common.registry.HatItemRegistry;

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
 * Main mod class for Astryxion's Hats
 * Forge 1.20.1 clean entry
 */
@Mod(AstryxionsHats.MODID)
public class AstryxionsHats {

    public static final String MODID = "astryxionshats";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AstryxionsHats() {

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

        // =========================
        // Lifecycle
        // =========================

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Astryxion's Hats initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        // IMPORTANT: register packets after everything loads
        event.enqueueWork(HatPacketHandler::register);

        LOGGER.info("Astryxion's Hats common setup complete.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        LOGGER.info("Astryxion's Hats server starting.");
    }
}
