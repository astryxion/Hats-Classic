package com.astryxion.hats.common.capability;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatPart;
import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.hat.PlayerHatData;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketSyncHat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = Hats.MODID)
public class HatCloneHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player clone = event.getEntity();

        PlayerHatData oldData = original.getData(HatDataCapability.HAT_DATA);
        PlayerHatData newData = clone.getData(HatDataCapability.HAT_DATA);
        newData.copyFrom(oldData);

        HatPart oldPart = original.getData(HatPartCapability.HAT_PART);
        HatPart newPart = clone.getData(HatPartCapability.HAT_PART);
        newPart.setHatStack(oldPart.getHatStack());
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
        HatPacketHandler.syncPlayerHatPartToTracking(player);

        // Sync GUI Unlocks (1 argument: NBT)
        PlayerHatData data = player.getData(HatDataCapability.HAT_DATA);
        HatPacketHandler.sendToPlayer(player, new PacketSyncHat(data.serializeNBT()));
        com.astryxion.hats.common.network.PacketSetUnlockedHats.send(
                player, data.getUnlockedHats(), data.getEquippedHat());
    }
}