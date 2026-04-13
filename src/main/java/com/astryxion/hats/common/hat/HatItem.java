package com.astryxion.hats.common.hat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class HatItem extends Item {

    private final String hatId;

    public HatItem(String hatId) {
        super(new Item.Properties().stacksTo(1));
        this.hatId = hatId;
    }

    public Rarity getRarity(ItemStack stack, Level level) {
        HatRarity rarity = HatRarityLoader.get(hatId);

        return switch (rarity) {
            case EPIC -> Rarity.EPIC;
            case RARE -> Rarity.RARE;
            case UNCOMMON -> Rarity.UNCOMMON;
            default -> Rarity.COMMON;
        };
    }
}
