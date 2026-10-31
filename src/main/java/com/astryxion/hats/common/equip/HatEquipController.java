package com.astryxion.hats.common.equip;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketEquipHat;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


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

        equipStack(player, new ItemStack(hat));
    }

    public static void equipStack(Player player, ItemStack stack) {

        if (player == null || stack.isEmpty())
            return;

        if (BuiltInRegistries.ITEM.getKey(stack.getItem()) == null)
            return;

        HatPacketHandler.sendToServer(
                new PacketEquipHat(stack)
        );

        HatManager.setHatStack(player, stack.copy());
    }

    // ============================
    // Unequip
    // ============================

    public static void unequip(Player player) {

        if (player == null)
            return;

        HatPacketHandler.sendToServer(
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
