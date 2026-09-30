package com.astryxion.hats.common.hat;

public enum HatRarity {
    COMMON(0xFFFFFF),
    UNCOMMON(0x55FF55),
    RARE(0x5555FF),
    EPIC(0xFF55FF),
    LEGENDARY(0xFFAA00);

    private final int color;

    HatRarity(int color) {
        this.color = color;
    }

    public int getColor() {
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
