package com.astryxion.hats.client.tabula;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.tabula.TabulaHatDefinition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public final class TabulaHatTextureManager {

    private static final Map<String, Identifier> CACHE = new HashMap<>();

    private TabulaHatTextureManager() {}

    public static Identifier getTexture(TabulaHatDefinition def) {
        if (def == null) {
            return null;
        }
        byte[] png = def.textureBytes();
        if (png == null || png.length == 0) {
            return null;
        }
        return CACHE.computeIfAbsent(def.id, id -> upload(id, png));
    }

    private static Identifier upload(String id, byte[] pngBytes) {
        try {
            com.mojang.blaze3d.platform.NativeImage image =
                    com.mojang.blaze3d.platform.NativeImage.read(new ByteArrayInputStream(pngBytes));
            DynamicTexture texture = new DynamicTexture(() -> "hats_tabula_" + id, image);
            Identifier location = Identifier.fromNamespaceAndPath(Hats.MODID, "dynamic/tabula/" + Integer.toUnsignedString(id.hashCode()));
            Minecraft.getInstance().getTextureManager().register(location, texture);
            return location;
        } catch (Exception e) {
            Hats.LOGGER.error("Failed to upload tabula hat texture '{}'", id, e);
            return null;
        }
    }

    public static void clearAll() {
        for (Identifier loc : CACHE.values()) {
            Minecraft.getInstance().getTextureManager().release(loc);
        }
        CACHE.clear();
    }
}
