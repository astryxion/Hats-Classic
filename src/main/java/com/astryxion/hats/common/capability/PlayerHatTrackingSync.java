package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * When a client starts tracking another player, send that player's hat render state.
 * Without this, join order can leave remote players with empty {@code HatPart} on the client.
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
        if (!(target instanceof Player tracked) || tracked == tracker) {
            return;
        }
        HatPacketHandler.sendPlayerHatPartTo(tracker, tracked);
    }
}
