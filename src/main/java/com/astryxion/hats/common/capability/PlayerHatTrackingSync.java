package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * When a client starts tracking an entity, send that entity's hat. Without this,
 * hats live only in the custom slot on the server and never appear on the client.
 */
@Mod.EventBusSubscriber(modid = Hats.MODID)
public final class PlayerHatTrackingSync {

    private PlayerHatTrackingSync() {}

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)) {
            return;
        }
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity living) || living == tracker) {
            return;
        }
        HatPacketHandler.sendHatPartTo(tracker, living);
    }
}
