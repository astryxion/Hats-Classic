package com.astryxion.hats.client.compat;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

import java.util.List;

/**
 * When 3D Skin Layers is installed, player hats use a 1.1 scale so they cover
 * the voxel head shell. Mobs and hat-less players stay at the authored 1.0.
 */
public final class SkinLayers3dHatCompat {

    private static final String MOD_ID = "skinlayers3d";
    private static final String HEAD_LAYER_CLASS =
            "dev.tr7zw.skinlayers.renderlayers.HeadLayerFeatureRenderer";
    private static final String CUSTOM_LAYER_CLASS =
            "dev.tr7zw.skinlayers.renderlayers.CustomLayerFeatureRenderer";

    public static final float PLAYER_HAT_SCALE = 1.1F;

    private static Boolean present;

    private SkinLayers3dHatCompat() {
    }

    public static boolean isLoaded() {
        if (present == null) {
            present = ModList.get().isLoaded(MOD_ID);
        }
        return present;
    }

    public static float playerHatScale(LivingEntity entity) {
        if (!isLoaded() || !(entity instanceof AbstractClientPlayer)) {
            return 1.0F;
        }
        return PLAYER_HAT_SCALE;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void moveHatLayerAfterSkinHead(List<?> layers, Object hatLayer) {
        if (!isLoaded() || layers.isEmpty() || !layers.contains(hatLayer)) {
            return;
        }

        int skinHeadIndex = indexOfSkinHeadLayer(layers);
        int hatIndex = layers.indexOf(hatLayer);
        if (skinHeadIndex < 0 || hatIndex == skinHeadIndex + 1) {
            return;
        }

        List mutable = (List) layers;
        mutable.remove(hatLayer);
        skinHeadIndex = indexOfSkinHeadLayer(layers);
        if (skinHeadIndex >= 0) {
            mutable.add(skinHeadIndex + 1, hatLayer);
        } else {
            mutable.add(hatLayer);
        }
    }

    private static int indexOfSkinHeadLayer(List<?> layers) {
        for (int i = 0; i < layers.size(); i++) {
            String className = layers.get(i).getClass().getName();
            if (HEAD_LAYER_CLASS.equals(className) || CUSTOM_LAYER_CLASS.equals(className)) {
                return i;
            }
        }
        return -1;
    }
}
