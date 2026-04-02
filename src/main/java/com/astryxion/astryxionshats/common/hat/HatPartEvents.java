package com.astryxion.astryxionshats.common.hat;

import com.astryxion.astryxionshats.AstryxionsHats;
import com.astryxion.astryxionshats.common.capability.HatDataCapability;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;
import com.astryxion.astryxionshats.common.network.PacketSyncHat;

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
@Mod.EventBusSubscriber(modid = AstryxionsHats.MODID)
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
            player.getCapability(HatDataCapability.HAT_DATA).ifPresent(cap -> {
                // Send the saved NBT data to the client so the GUI is populated
                HatPacketHandler.sendToPlayer(
                        player,
                        new PacketSyncHat(cap.serializeNBT())
                );
            });
        }
    }
}