package com.astryxion.astryxionshats.common.capability;

import com.astryxion.astryxionshats.common.hat.HatPartCapability;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;
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
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        syncEverything(player);
    }

    public static void onDimensionChange(ServerPlayer player) {
        syncEverything(player);
    }

    private static void syncEverything(ServerPlayer player) {
        var part = HatPartCapability.get(player);
        if (part != null) {
            HatPacketHandler.sendSyncHatPartToPlayer(player, player.getId(), part.serializeNBT(player.getServer().registryAccess()));
        }

        HatDataCapability.get(player).ifPresent(data -> {
            HatPacketHandler.sendSyncHatToPlayer(player, data.serializeNBT());
        });
    }
}
