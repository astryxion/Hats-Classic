package com.astryxion.hats.common.capability;

import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.hat.HatPartRestore;
import com.astryxion.hats.common.network.HatPacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class HatCloneHandler {

    public static void onPlayerClone(Player original, Player clone, boolean alive) {
        HatDataCapability.get(original).ifPresent(oldData -> {
            HatDataCapability.get(clone).ifPresent(newData -> {
                newData.copyFrom(oldData);
                HatDataCapability.markDirty(clone);
            });
        });

        var oldPart = HatPartCapability.get(original);
        var newPart = HatPartCapability.getOrCreate(clone);
        if (oldPart != null && newPart != null) {
            newPart.setHatStack(oldPart.getHatStack());
        }
        if (clone instanceof ServerPlayer serverClone) {
            HatPartRestore.restoreFromSavedData(serverClone);
        }
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        syncEverything(player);
    }

    public static void onDimensionChange(ServerPlayer player) {
        syncEverything(player);
    }

    private static void syncEverything(ServerPlayer player) {
        HatPartRestore.restoreFromSavedData(player);
        var part = HatPartCapability.getOrCreate(player);
        HatPacketHandler.sendSyncHatPartToTracking(
                player,
                player.getId(),
                part.serializeNBT(player.registryAccess()));

        HatDataCapability.get(player).ifPresent(data -> {
            HatPacketHandler.sendSyncHatToPlayer(player, data.serializeNBT());
        });
    }
}
