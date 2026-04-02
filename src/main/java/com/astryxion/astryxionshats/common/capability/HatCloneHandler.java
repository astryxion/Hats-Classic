package com.astryxion.astryxionshats.common.capability;

import com.astryxion.astryxionshats.AstryxionsHats;
import com.astryxion.astryxionshats.common.hat.HatPartCapability;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;
import com.astryxion.astryxionshats.common.network.PacketSyncHat;
import com.astryxion.astryxionshats.common.network.PacketSyncHatPart;
import com.astryxion.astryxionshats.common.hat.PlayerHatData;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AstryxionsHats.MODID)
public class HatCloneHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player clone = event.getEntity();

        // 🔧 CRITICAL: Revive capabilities so we can actually read the old data during death
        if (event.isWasDeath()) {
            original.reviveCaps();
        }

        // 1. Move the Unlock Collection (Memory / GUI)
        original.getCapability(HatDataCapability.HAT_DATA).ifPresent(oldData -> {
            clone.getCapability(HatDataCapability.HAT_DATA).ifPresent(newData -> {
                newData.copyFrom(oldData);
            });
        });

        // 2. Move the Visual Hat (Equipped Slot)
        original.getCapability(HatPartCapability.HAT_PART).ifPresent(oldPart -> {
            clone.getCapability(HatPartCapability.HAT_PART).ifPresent(newPart -> {
                newPart.setHatStack(oldPart.getHatStack());
            });
        });

        // 🔧 Clean up to prevent memory leaks
        if (event.isWasDeath()) {
            original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncEverything(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncEverything(player);
        }
    }

    // Helper to keep the code clean and ensure both GUI and Visuals sync together
    private static void syncEverything(ServerPlayer player) {
        // Sync Visual Hat (2 arguments: ID and NBT)
        player.getCapability(HatPartCapability.HAT_PART).ifPresent(part -> {
            HatPacketHandler.sendToPlayer(player, new PacketSyncHatPart(player.getId(), part.serializeNBT()));
        });

        // Sync GUI Unlocks (1 argument: NBT)
        player.getCapability(HatDataCapability.HAT_DATA).ifPresent(data -> {
            HatPacketHandler.sendToPlayer(player, new PacketSyncHat(data.serializeNBT()));
        });
    }
}