package com.astryxion.hats.client.gui.widget;

import com.astryxion.hats.Hats;

import net.minecraft.resources.ResourceLocation;

/**
 * Icon textures used by Hats 1.16.5 sidebar / list widgets.
 */
public final class HatsIcons {

    public static final ResourceLocation CANCEL = icon("cancel");
    public static final ResourceLocation COLOURISE = icon("colourise");
    public static final ResourceLocation RANDOMISE = icon("randomise");
    public static final ResourceLocation CATEGORIES = icon("categories");
    public static final ResourceLocation SEARCH = icon("search");
    public static final ResourceLocation FAVOURITE = icon("favourite");
    public static final ResourceLocation HAT = icon("hat");
    public static final ResourceLocation PERSONALISE = icon("personalise");
    public static final ResourceLocation TRADE = icon("trade");

    private HatsIcons() {}

    private static ResourceLocation icon(String name) {
        return new ResourceLocation(Hats.MODID, "textures/icon/" + name + ".png");
    }
}
