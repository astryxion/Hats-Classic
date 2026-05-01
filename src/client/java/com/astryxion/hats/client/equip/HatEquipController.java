package com.astryxion.hats.client.equip;

import com.astryxion.hats.client.network.HatClientPackets;
import com.astryxion.hats.common.hat.HatManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class HatEquipController {

    private HatEquipController() {}

    public static void equip(Player player, Item hat) {

        if (player == null || hat == null)
            return;

        Identifier id = BuiltInRegistries.ITEM.getKey(hat);

        HatClientPackets.sendEquipToServer(id.toString());

        HatManager.setHatStack(player, new ItemStack(hat));
    }

    public static void equipStack(Player player, ItemStack stack) {

        if (player == null || stack.isEmpty())
            return;

        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        HatClientPackets.sendEquipToServer(id.toString());

        HatManager.setHatStack(player, stack.copy());
    }

    public static void unequip(Player player) {

        if (player == null)
            return;

        HatClientPackets.sendEquipToServer("none");

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
