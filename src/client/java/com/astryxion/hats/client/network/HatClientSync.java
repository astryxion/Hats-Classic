package com.astryxion.hats.client.network;

import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.HatManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Client-side fallback so the local player's hat model matches synced metadata
 * when hat-part packets arrive late or were skipped.
 */
public final class HatClientSync {

    private HatClientSync() {}

    public static void applyLocalEquippedHat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        HatDataCapability.get(mc.player).ifPresent(data -> {
            Identifier equippedId = data.getEquippedHat();
            if (equippedId == null) {
                HatManager.clearHat(mc.player);
                return;
            }
            Item item = BuiltInRegistries.ITEM.getValue(ResourceKey.create(Registries.ITEM, equippedId));
            if (item == null || item == Items.AIR) {
                HatManager.clearHat(mc.player);
                return;
            }
            HatManager.setHatStack(mc.player, new ItemStack(item));
        });
    }
}
