package com.astryxion.hats.common.hat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Enchanted-item shimmer stored on an equipped hat stack, same as RGB.
 */
public final class HatGlint {

    public static final String TAG = "HatsEnchanted";

    private HatGlint() {}

    public static boolean get(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBooleanOr(TAG, false);
    }

    public static void set(ItemStack stack, boolean enchanted) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (enchanted) {
            tag.putBoolean(TAG, true);
        } else {
            tag.remove(TAG);
        }
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
