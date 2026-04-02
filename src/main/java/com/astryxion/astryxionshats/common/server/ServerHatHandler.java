package com.astryxion.astryxionshats.common.server;

import com.astryxion.astryxionshats.common.hat.HatManager;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;
import com.astryxion.astryxionshats.common.capability.HatDataCapability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ServerHatHandler {

    private ServerHatHandler() {}

    public static void handleEquip(ServerPlayer player, String hatId) {

        if (player == null)
            return;

        if (hatId.equals("none")) {

            HatManager.clearHat(player);

            HatDataCapability.get(player).ifPresent(data -> {
                data.setEquippedHat(null);
                HatDataCapability.markDirty(player);
                HatPacketHandler.sendSyncHatToPlayer(
                        player,
                        data.serializeNBT()
                );
            });

            return;
        }

        ResourceLocation id = ResourceLocation.tryParse(hatId);
        if (id == null)
            return;

        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == net.minecraft.world.item.Items.AIR)
            return;

        ItemStack stack = new ItemStack(item);
        HatManager.setHatStack(player, stack);

        HatDataCapability.get(player).ifPresent(data -> {
            data.setEquippedHat(id);
            HatDataCapability.markDirty(player);
            HatPacketHandler.sendSyncHatToPlayer(
                    player,
                    data.serializeNBT()
            );
        });
    }
}
