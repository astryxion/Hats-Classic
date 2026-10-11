package com.astryxion.hats.common.hat;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;


/**
 * Detects (C) contributor hats via Tabula path or lang key.
 * Names use rarity colors like other hats; the (C) prefix is the marker.
 */
public final class HatContributor {

    private HatContributor() {
    }

    public static boolean isContributor(Item item) {
        if (item == null) {
            return false;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return isContributor(item, id);
    }

    public static boolean isContributor(Item item, Identifier hatId) {
        if (item instanceof HatItem hatItem && hatItem.isTabula()) {
            TabulaHatDefinition def = TabulaHats.get(hatItem.getHatId());
            if (def != null && def.relativePath.replace('\\', '/').toLowerCase().startsWith("contributors/")) {
                return true;
            }
        }
        if (hatId == null) {
            return false;
        }
        String key = "item.hats.contributors_" + hatId.getPath();
        return !Component.translatable(key).getString().equals(key);
    }
}
