package com.astryxion.hats.common.server;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.HatAdvancements;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketSyncHat;

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
        ResourceLocation id = ResourceLocation.tryParse(hatId);
        Item item = id != null ? ForgeRegistries.ITEMS.getValue(id) : null;
        handleEquip(player, item == null || "none".equals(hatId) ? ItemStack.EMPTY : new ItemStack(item));
    }

    public static void handleEquip(ServerPlayer player, ItemStack requestedHat) {

        if (player == null)
            return;

        // =====================
        // UNEQUIP
        // =====================

        if (requestedHat == null || requestedHat.isEmpty()) {

            HatManager.clearHat(player);
            HatPacketHandler.syncPlayerHatPartToTracking(player);

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

        Item item = requestedHat.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || !Hats.MODID.equals(id.getNamespace()))
            return;

        // Equip on server (Visual/Part layer)
        ItemStack stack = requestedHat.copy();
        stack.setCount(1);
        HatManager.setHatStack(player, stack);
        HatPacketHandler.syncPlayerHatPartToTracking(player);
        HatAdvancements.onEquip(player, stack);

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