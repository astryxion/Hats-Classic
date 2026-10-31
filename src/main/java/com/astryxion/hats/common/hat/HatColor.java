package com.astryxion.hats.common.hat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
        if (stack == null || stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA)) {
            return DEFAULT;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(TAG) ? tag.getIntOr(TAG, 0) & 0xFFFFFF : DEFAULT;
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
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
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
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
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
        if (stack == null || stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA)) {
            return 255;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(ALPHA_TAG) ? clamp(tag.getIntOr(ALPHA_TAG, 255)) : 255;
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
