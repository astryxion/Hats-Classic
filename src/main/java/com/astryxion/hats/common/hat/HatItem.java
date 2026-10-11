package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import org.jetbrains.annotations.Nullable;

public class HatItem extends Item {

    private final String hatId;
    private final String tabulaPath;

    public HatItem(String hatId) {
        this(hatId, null);
    }

    public HatItem(String hatId, String tabulaPath) {
        super(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Hats.MODID, hatId)))
                .stacksTo(1));
        this.hatId = hatId;
        this.tabulaPath = tabulaPath;
    }

    public String getHatId() {
        return hatId;
    }

    public boolean isTabula() {
        return tabulaPath != null;
    }

    public String getTabulaPath() {
        return tabulaPath;
    }

    public HatRarity getHatRarity() {
        if (tabulaPath != null) {
            TabulaHatDefinition def = TabulaHats.get(hatId);
            if (def != null) {
                return def.rarity;
            }
        }
        return HatRarityLoader.get(hatId);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component named = tabulaDisplayName();
        Component base = named != null ? named : super.getName(stack);
        return base.copy().withStyle(style -> style.withColor(getHatRarity().getColor()));
    }

    @Nullable
    private Component tabulaDisplayName() {
        if (tabulaPath == null) {
            return null;
        }
        Component translated = Component.translatable(getDescriptionId());
        if (!translated.getString().equals(getDescriptionId())) {
            return translated;
        }
        String contributorKey = "item.hats.contributors_" + hatId;
        Component contributor = Component.translatable(contributorKey);
        if (!contributor.getString().equals(contributorKey)) {
            return contributor;
        }
        TabulaHatDefinition def = TabulaHats.get(hatId);
        if (def != null && def.displayName != null && !def.displayName.isEmpty()) {
            return Component.literal(def.displayName);
        }
        return null;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return HatGlint.get(stack) || super.isFoil(stack);
    }

    public Rarity getRarity(ItemStack stack) {
        HatRarity rarity = getHatRarity();

        return switch (rarity) {
            case LEGENDARY -> Rarity.EPIC;
            case EPIC -> Rarity.EPIC;
            case RARE -> Rarity.RARE;
            case UNCOMMON -> Rarity.UNCOMMON;
            default -> Rarity.COMMON;
        };
    }
}
