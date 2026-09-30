package com.astryxion.hats.common.hat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Enchanted-item shimmer stored on an equipped hat stack, same as RGB.
 */
public final class HatGlint {

    public static final String TAG = "HatsEnchanted";

    private HatGlint() {}

    public static boolean get(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean(TAG);
    }

    public static void set(ItemStack stack, boolean enchanted) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (enchanted) {
            tag.putBoolean(TAG, true);
        } else {
            tag.remove(TAG);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}
