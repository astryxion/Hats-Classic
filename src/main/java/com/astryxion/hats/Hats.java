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
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/**
 * Main mod class for Astryxion's Hats
 * NeoForge 1.21.1 entry
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

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLoggedIn(handler.getPlayer()));
        ServerLivingEntityEvents.AFTER_DEATH.register(this::onLivingDeath);
        ServerPlayerEvents.COPY_FROM.register(this::onPlayerClone);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> onPlayerRespawn(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(this::onPlayerChangeDimension);
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);

        LOGGER.info("Astryxion's Hats common setup complete.");
    }

    private void onPlayerLoggedIn(ServerPlayer player) {
        HatPartEvents.onPlayerJoinLevel(player);
        HatLoginHandler.onPlayerLogin(player);
        HatWelcomeHandler.onPlayerJoin(player);
    }

    private void onLivingDeath(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        HatUnlockHandler.onMobKilled(entity, source);
    }

    private void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        HatCloneHandler.onPlayerClone(oldPlayer, newPlayer, alive);
    }

    private void onPlayerRespawn(ServerPlayer player) {
        HatCloneHandler.onPlayerRespawn(player);
    }

    private void onPlayerChangeDimension(ServerPlayer player, net.minecraft.server.level.ServerLevel origin, net.minecraft.server.level.ServerLevel destination) {
        HatCloneHandler.onDimensionChange(player);
    }

    private void onServerStarting(net.minecraft.server.MinecraftServer server) {
        LOGGER.info("Astryxion's Hats server starting.");
    }
}
