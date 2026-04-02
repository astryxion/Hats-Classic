package com.astryxion.astryxionshats;

import com.astryxion.astryxionshats.common.capability.HatCloneHandler;
import com.astryxion.astryxionshats.common.capability.HatLoginHandler;
import com.astryxion.astryxionshats.common.events.HatWelcomeHandler;
import com.astryxion.astryxionshats.common.hat.HatPartEvents;
import com.astryxion.astryxionshats.common.hat.HatRarityLoader;
import com.astryxion.astryxionshats.common.hat.HatUnlockHandler;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;
import com.astryxion.astryxionshats.common.registry.HatItemRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

/**
 * Main mod class for Astryxion's Hats
 * NeoForge 1.21.1 entry
 */
@Mod(AstryxionsHats.MODID)
public class AstryxionsHats {

    public static final String MODID = "astryxionshats";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AstryxionsHats(IEventBus modEventBus) {
        Config.load();

        HatItemRegistry.register(modEventBus);

        HatRarityLoader.load();
        HatPacketHandler.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(this::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerChangeDimension);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.astryxion.astryxionshats.client.AstryxionsHatsClient.registerClient(modEventBus);
        }

        LOGGER.info("Astryxion's Hats common setup complete.");
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            HatPartEvents.onPlayerJoinLevel(player);
            HatLoginHandler.onPlayerLogin(player);
            HatWelcomeHandler.onPlayerJoin(player);
        }
    }

    private void onLivingDeath(LivingDeathEvent event) {
        HatUnlockHandler.onMobKilled(event.getEntity(), event.getSource());
    }

    private void onPlayerClone(PlayerEvent.Clone event) {
        HatCloneHandler.onPlayerClone(event.getOriginal(), event.getEntity(), event.isWasDeath());
    }

    private void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            HatCloneHandler.onPlayerRespawn(player);
        }
    }

    private void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            HatCloneHandler.onDimensionChange(player);
        }
    }

    private void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Astryxion's Hats server starting.");
    }
}
