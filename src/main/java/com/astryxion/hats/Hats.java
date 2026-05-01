package com.astryxion.hats;

import com.astryxion.hats.common.capability.HatCloneHandler;
import com.astryxion.hats.common.capability.HatLoginHandler;
import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.events.HatWelcomeHandler;
import com.astryxion.hats.common.hat.HatPartEvents;
import com.astryxion.hats.common.hat.HatRarityLoader;
import com.astryxion.hats.common.hat.HatUnlockHandler;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.registry.HatItemRegistry;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/**
 * Main mod class for Astryxion's Hats
 * Fabric 26.1 entry
 */
public class Hats implements ModInitializer {

    public static final String MODID = "hats";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        Config.load();

        HatItemRegistry.register();

        HatRarityLoader.load();
        HatPacketHandler.register();

        ServerPlayerEvents.JOIN.register(player -> {
            HatPartEvents.onPlayerJoinLevel(player);
            HatLoginHandler.onPlayerLogin(player);
            HatWelcomeHandler.onPlayerJoin(player);
        });

        /**
         * When a client first tracks another player (e.g. comes into range), apply that player's hat.
         * Login-time sync can run before the remote entity exists on the client, so this covers late discovery.
         */
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, trackerPlayer) -> {
            if (!(trackerPlayer instanceof ServerPlayer tracker)) {
                return;
            }
            if (!(trackedEntity instanceof ServerPlayer tracked)) {
                return;
            }
            var part = HatPartCapability.get(tracked);
            if (part != null) {
                HatPacketHandler.sendSyncHatPartToPlayer(
                        tracker,
                        tracked.getId(),
                        part.serializeNBT(tracker.registryAccess()));
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) ->
                HatUnlockHandler.onMobKilled(entity, damageSource));

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
                HatCloneHandler.onPlayerClone(oldPlayer, newPlayer, !alive));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer != null) {
                HatCloneHandler.onPlayerRespawn(newPlayer);
            }
        });

        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) ->
                HatCloneHandler.onDimensionChange(player));

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                LOGGER.info("Astryxion's Hats server starting."));

        LOGGER.info("Astryxion's Hats common setup complete.");
    }
}
