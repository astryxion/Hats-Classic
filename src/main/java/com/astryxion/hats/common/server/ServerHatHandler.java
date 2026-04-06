package com.astryxion.hats.common.server;

import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketSyncHat;
import com.astryxion.hats.common.capability.HatDataCapability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;

/**
 * Server-side hat equip + sync handler
 */
public final class ServerHatHandler {

    private ServerHatHandler() {}

    // =========================
    // EQUIP HANDLER (FROM PACKET)
    // =========================

    public static void handleEquip(ServerPlayer player, String hatId) {

        if (player == null)
            return;

        // =====================
        // UNEQUIP
        // =====================

        if (hatId.equals("none")) {

            HatManager.clearHat(player);

            // 🔧 FIXED: Tell the capability memory we are wearing nothing
            player.getCapability(HatDataCapability.HAT_DATA).ifPresent(data -> {
                data.setEquippedHat(null); // Clear the saved hat

                HatPacketHandler.sendToPlayer(
                        player,
                        new PacketSyncHat(data.serializeNBT())
                );
            });

            return;
        }

        // =====================
        // EQUIP
        // =====================

        ResourceLocation id = ResourceLocation.tryParse(hatId);
        if (id == null)
            return;

        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null)
            return;

        // Equip on server (Visual/Part layer)
        ItemStack stack = new ItemStack(item);
        HatManager.setHatStack(player, stack);

        // 🔧 FIXED: Tell the capability memory exactly which hat we are wearing
        player.getCapability(HatDataCapability.HAT_DATA).ifPresent(data -> {
            data.setEquippedHat(id); // Save the hat ID to capability memory

            // Sync FULL capability data (This ensures the GUI stays in sync)
            HatPacketHandler.sendToPlayer(
                    player,
                    new PacketSyncHat(data.serializeNBT())
            );
        });
    }
}