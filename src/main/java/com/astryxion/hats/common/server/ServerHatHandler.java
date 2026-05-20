package com.astryxion.hats.common.server;

import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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

            var clearedPart = HatPartCapability.getOrCreate(player);
            HatPacketHandler.sendSyncHatPartToTracking(
                    player,
                    player.getId(),
                    clearedPart.serializeNBT(player.registryAccess()));

            return;
        }

        Identifier id = Identifier.tryParse(hatId);
        if (id == null)
            return;

        Item item = BuiltInRegistries.ITEM.getValue(ResourceKey.create(Registries.ITEM, id));
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

        var equippedPart = HatPartCapability.getOrCreate(player);
        HatPacketHandler.sendSyncHatPartToTracking(
                player,
                player.getId(),
                equippedPart.serializeNBT(player.registryAccess()));
    }
}
