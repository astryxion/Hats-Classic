package com.astryxion.hats.common.hat;

import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import net.minecraftforge.registries.ForgeRegistries;

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
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return isContributor(item, id);
    }

    public static boolean isContributor(Item item, ResourceLocation hatId) {
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
