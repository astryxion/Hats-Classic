package com.astryxion.hats.common.equip;

import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class HatEquipController {

    private HatEquipController() {}

    public static void equip(Player player, Item hat) {

        if (player == null || hat == null)
            return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(hat);

        HatPacketHandler.sendEquipToServer(id.toString());

        HatManager.setHatStack(player, new ItemStack(hat));
    }

    public static void equipStack(Player player, ItemStack stack) {

        if (player == null || stack.isEmpty())
            return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        HatPacketHandler.sendEquipToServer(id.toString());

        HatManager.setHatStack(player, stack.copy());
    }

    public static void unequip(Player player) {

        if (player == null)
            return;

        HatPacketHandler.sendEquipToServer("none");

        HatManager.clearHat(player);
    }

    public static boolean hasHat(Player player) {
        return player != null && HatManager.hasHat(player);
    }

    public static ItemStack getEquipped(Player player) {

        if (player == null)
            return ItemStack.EMPTY;

        return HatManager.getHat(player);
    }
}
