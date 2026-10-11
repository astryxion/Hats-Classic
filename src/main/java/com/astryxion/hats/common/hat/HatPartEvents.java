package com.astryxion.hats.common.hat;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketSyncHat;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Handles capability attachment and initial data syncing
 */
@EventBusSubscriber(modid = Hats.MODID)
public class HatPartEvents {

    // ==========================================
    // RELOG FIX: Sync data when player joins
    // ==========================================
    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        // Only sync on the server side
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            com.astryxion.hats.common.data.ServerHatUnlocks store =
                    com.astryxion.hats.common.data.ServerHatUnlocks.get(player);
            store.ensureSeeded(player);
            java.util.List<net.minecraft.resources.Identifier> unlocked =
                    java.util.List.copyOf(store.copy(player));
            net.minecraft.resources.Identifier equipped =
                    com.astryxion.hats.common.trade.TradeHats.equippedId(player);
            com.astryxion.hats.common.capability.HatDataAccess.replaceUnlocked(player, unlocked, equipped);
            HatPacketHandler.sendToPlayer(player, new PacketSyncHat(
                    com.astryxion.hats.common.capability.HatDataAccess.primaryNbt(player)));
            com.astryxion.hats.common.network.PacketSetUnlockedHats.send(player, unlocked, equipped);
        }
    }
}