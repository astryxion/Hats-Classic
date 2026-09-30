package com.astryxion.hats.common.hat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * RGB color stored on an equipped hat stack. Hat models opt into coloring with
 * vanilla {@code tintindex: 0}, matching the colorizable parts of the source models.
 */
public final class HatColor {

    public static final String TAG = "HatsColor";
    public static final String ALPHA_TAG = "HatsAlpha";
    public static final int DEFAULT = 0xFFFFFF;

    private HatColor() {}

    public static int get(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return DEFAULT;
        }
        return stack.getTag().contains(TAG) ? stack.getTag().getInt(TAG) & 0xFFFFFF : DEFAULT;
    }

    public static void set(ItemStack stack, int red, int green, int blue) {
        set(stack, red, green, blue, alpha(stack));
    }

    public static void set(ItemStack stack, int red, int green, int blue, int alpha) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int color = (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
        int clampedAlpha = clamp(alpha);
        CompoundTag tag = stack.getOrCreateTag();
        if (color == DEFAULT) {
            tag.remove(TAG);
        } else {
            tag.putInt(TAG, color);
        }
        if (clampedAlpha == 255) {
            tag.remove(ALPHA_TAG);
        } else {
            tag.putInt(ALPHA_TAG, clampedAlpha);
        }
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static int red(ItemStack stack) {
        return get(stack) >> 16 & 0xFF;
    }

    public static int green(ItemStack stack) {
        return get(stack) >> 8 & 0xFF;
    }

    public static int blue(ItemStack stack) {
        return get(stack) & 0xFF;
    }

    public static int alpha(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return 255;
        }
        return stack.getTag().contains(ALPHA_TAG) ? clamp(stack.getTag().getInt(ALPHA_TAG)) : 255;
    }

    public static int argb(ItemStack stack) {
        return alpha(stack) << 24 | get(stack);
    }

    public static boolean isDefault(ItemStack stack) {
        return get(stack) == DEFAULT && alpha(stack) == 255;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
