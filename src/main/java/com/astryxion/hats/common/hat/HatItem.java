package com.astryxion.hats.common.hat;

import com.astryxion.hats.client.tabula.TabulaHatClientExtensions;
import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HatItem extends Item {

    private final String hatId;
    private final String tabulaPath;

    public HatItem(String hatId) {
        this(hatId, null);
    }

    public HatItem(String hatId, String tabulaPath) {
        super(new Item.Properties().stacksTo(1));
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
    public Component getDescription() {
        Component named = tabulaDisplayName();
        return named != null ? named : super.getDescription();
    }

    @Override
    public Component getName(ItemStack stack) {
        Component named = tabulaDisplayName();
        return named != null ? named : super.getName(stack);
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
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        if (tabulaPath == null) {
            return;
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TabulaHatClientExtensions.register(consumer));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return HatGlint.get(stack) || super.isFoil(stack);
    }

    @Override
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
