package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketSyncHat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles capability attachment and initial data syncing
 */
@Mod.EventBusSubscriber(modid = Hats.MODID)
public class HatPartEvents {

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(
                    HatPartProvider.ID,
                    new HatPartProvider()
            );
        }
    }

    // ==========================================
    // RELOG FIX: Sync data when player joins
    // ==========================================
    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        // Only sync on the server side
        if (event.getLevel().isClientSide) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            com.astryxion.hats.common.data.ServerHatUnlocks store =
                    com.astryxion.hats.common.data.ServerHatUnlocks.get(player);
            store.ensureSeeded(player);
            java.util.List<net.minecraft.resources.ResourceLocation> unlocked =
                    java.util.List.copyOf(store.copy(player));
            net.minecraft.resources.ResourceLocation equipped =
                    com.astryxion.hats.common.trade.TradeHats.equippedId(player);
            com.astryxion.hats.common.capability.HatDataAccess.replaceUnlocked(player, unlocked, equipped);
            HatPacketHandler.sendToPlayer(player, new PacketSyncHat(
                    com.astryxion.hats.common.capability.HatDataAccess.primaryNbt(player)));
            com.astryxion.hats.common.network.PacketSetUnlockedHats.send(player, unlocked, equipped);
        }
    }
}