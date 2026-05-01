package com.astryxion.hats;

import com.astryxion.hats.common.capability.HatCloneHandler;
import com.astryxion.hats.common.capability.HatLoginHandler;
import com.astryxion.hats.common.events.HatWelcomeHandler;
import com.astryxion.hats.common.hat.HatPartEvents;
import com.astryxion.hats.common.hat.HatRarityLoader;
import com.astryxion.hats.common.hat.HatUnlockHandler;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.registry.HatItemRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
@Mod(Hats.MODID)
public class Hats {

    public static final String MODID = "hats";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Hats(IEventBus modEventBus) {
        Config.load();

        HatItemRegistry.register(modEventBus);

        HatRarityLoader.load();
        HatPacketHandler.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(this::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerChangeDimension);
        NeoForge.EVENT_BUS.addListener(this::onPlayerStartTracking);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.astryxion.hats.client.HatsClient.registerClient(modEventBus);
        }

        LOGGER.info("Astryxion's Hats common setup complete.");
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
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
        if (event.getEntity() instanceof ServerPlayer player) {
            HatCloneHandler.onPlayerRespawn(player);
        }
    }

    private void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HatCloneHandler.onDimensionChange(player);
        }
    }

    /**
     * When a client starts tracking another player, send that player's hat render state.
     * Without this, join order can leave remote players with empty HatPart on the client.
     */
    private void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)) {
            return;
        }
        Entity target = event.getTarget();
        if (!(target instanceof Player tracked) || tracked == tracker) {
            return;
        }
        HatPacketHandler.sendPlayerHatPartTo(tracker, tracked);
    }

    private void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Astryxion's Hats server starting.");
    }
}
