package com.astryxion.hats;

import com.astryxion.hats.common.capability.HatCloneHandler;
import com.astryxion.hats.common.capability.HatLoginHandler;
import com.astryxion.hats.common.capability.PlayerHatTrackingSync;
import com.astryxion.hats.common.events.HatWelcomeHandler;
import com.astryxion.hats.common.hat.HatPartEvents;
import com.astryxion.hats.common.hat.HatRarityLoader;
import com.astryxion.hats.common.hat.HatUnlockHandler;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.registry.HatItemRegistry;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;

/**
 * Main mod class for Astryxion's Hats
 * Fabric 1.20.1 entry
 */
public class AstryxionsHats implements ModInitializer {

    public static final String MODID = "hats";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        Config.load();

        HatItemRegistry.register();

        HatRarityLoader.load();
        HatPacketHandler.register();
        PlayerHatTrackingSync.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            HatPartEvents.onPlayerJoinLevel(player);
            HatLoginHandler.onPlayerLogin(player);
            HatWelcomeHandler.onPlayerJoin(player);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            HatUnlockHandler.onMobKilled(entity, damageSource);
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            HatCloneHandler.onPlayerClone(oldPlayer, newPlayer, alive);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            HatCloneHandler.onPlayerRespawn(newPlayer);
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            HatCloneHandler.onDimensionChange(player);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                LOGGER.info("Astryxion's Hats server starting."));

        LOGGER.info("Astryxion's Hats common setup complete.");
    }
}
