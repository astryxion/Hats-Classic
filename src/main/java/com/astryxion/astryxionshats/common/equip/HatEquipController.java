package com.astryxion.astryxionshats.common.equip;

import com.astryxion.astryxionshats.common.hat.HatManager;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;
import com.astryxion.astryxionshats.common.network.PacketEquipHat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;

/**
 * Handles all hat equip/unequip logic
 * (GUI should ONLY call this class)
 */
public final class HatEquipController {

    private HatEquipController() {}

    // ============================
    // Equip
    // ============================

    public static void equip(Player player, Item hat) {

        if (player == null || hat == null)
            return;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(hat);

        if (id == null)
            return;

        // SEND TO SERVER (REAL EQUIP)
        HatPacketHandler.CHANNEL.sendToServer(
                new PacketEquipHat(id.toString())
        );

        // optional instant client feedback
        HatManager.setHatStack(player, new ItemStack(hat));
    }

    public static void equipStack(Player player, ItemStack stack) {

        if (player == null || stack.isEmpty())
            return;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (id == null)
            return;

        HatPacketHandler.CHANNEL.sendToServer(
                new PacketEquipHat(id.toString())
        );

        HatManager.setHatStack(player, stack.copy());
    }

    // ============================
    // Unequip
    // ============================

    public static void unequip(Player player) {

        if (player == null)
            return;

        HatPacketHandler.CHANNEL.sendToServer(
                new PacketEquipHat("none")
        );

        HatManager.clearHat(player);
    }

    // ============================
    // Query
    // ============================

    public static boolean hasHat(Player player) {
        return player != null && HatManager.hasHat(player);
    }

    public static ItemStack getEquipped(Player player) {

        if (player == null)
            return ItemStack.EMPTY;

        return HatManager.getHat(player);
    }
}
