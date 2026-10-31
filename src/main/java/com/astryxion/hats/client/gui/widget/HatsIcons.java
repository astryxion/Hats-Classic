package com.astryxion.hats.client.gui.widget;

import com.astryxion.hats.Hats;

import net.minecraft.resources.Identifier;

/**
 * Icon textures used by Hats 1.16.5 sidebar / list widgets.
 */
public final class HatsIcons {

    public static final Identifier CANCEL = icon("cancel");
    public static final Identifier COLOURISE = icon("colourise");
    public static final Identifier RANDOMISE = icon("randomise");
    public static final Identifier CATEGORIES = icon("categories");
    public static final Identifier SEARCH = icon("search");
    public static final Identifier FAVOURITE = icon("favourite");
    public static final Identifier HAT = icon("hat");
    public static final Identifier PERSONALISE = icon("personalise");
    public static final Identifier TRADE = icon("trade");

    private HatsIcons() {}

    private static Identifier icon(String name) {
        return Identifier.fromNamespaceAndPath(Hats.MODID, "textures/icon/" + name + ".png");
    }
}
