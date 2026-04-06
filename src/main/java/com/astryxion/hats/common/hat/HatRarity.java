package com.astryxion.hats.common.hat;

import net.minecraft.ChatFormatting;

public enum HatRarity {
    COMMON(ChatFormatting.WHITE),
    UNCOMMON(ChatFormatting.YELLOW),
    RARE(ChatFormatting.AQUA),
    EPIC(ChatFormatting.LIGHT_PURPLE);

    private final ChatFormatting color;

    HatRarity(ChatFormatting color) {
        this.color = color;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public static HatRarity fromString(String value) {
        try {
            return HatRarity.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return COMMON;
        }
    }
}
