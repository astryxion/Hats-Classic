package com.astryxion.hats.common.hat;

import com.astryxion.hats.common.capability.HatDataCapability;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Restores in-memory {@link HatPart} visuals from persisted {@link PlayerHatData#getEquippedHat()}.
 * HatPart is not saved to disk; without this, joins and restarts leave empty parts until re-equip.
 */
public final class HatPartRestore {

    private HatPartRestore() {}

    public static void restoreFromSavedData(ServerPlayer player) {
        if (player == null) {
            return;
        }
        HatDataCapability.get(player).ifPresent(data -> {
            Identifier equippedId = data.getEquippedHat();
            if (equippedId == null) {
                HatManager.clearHat(player);
                return;
            }
            Item item = BuiltInRegistries.ITEM.getValue(ResourceKey.create(Registries.ITEM, equippedId));
            if (item == null || item == Items.AIR) {
                HatManager.clearHat(player);
                return;
            }
            HatManager.setHatStack(player, new ItemStack(item));
        });
    }
}
