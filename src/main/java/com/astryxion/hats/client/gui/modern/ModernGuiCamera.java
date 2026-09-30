package com.astryxion.hats.client.gui.modern;

import com.astryxion.hats.client.gui.ModernHatsScreen;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public final class ModernGuiCamera {

    public static float hatMenuProgress;
    public static float hatMenuProgressPrev;
    public static float guiYaw;
    public static float guiPitch;
    public static float guiZoom;
    public static float guiPanX;
    public static float guiPanY;

    private static CameraType savedCameraType;
    private static final float SPEED = 0.07f;

    private ModernGuiCamera() {}

    public static void resetOrbit() {
        guiYaw = 0f;
        guiPitch = 0f;
        guiZoom = 0f;
        guiPanX = 0f;
        guiPanY = 0f;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        hatMenuProgressPrev = hatMenuProgress;
        boolean open = minecraft.screen instanceof ModernHatsScreen;

        if (open) {
            if (savedCameraType == null) {
                savedCameraType = minecraft.options.getCameraType();
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            }
            hatMenuProgress = Math.min(1f, hatMenuProgress + SPEED);
            return;
        }

        if (hatMenuProgress > 0f || savedCameraType != null) {
            hatMenuProgress = 0f;
            hatMenuProgressPrev = 0f;
            if (savedCameraType != null) {
                minecraft.options.setCameraType(savedCameraType);
                savedCameraType = null;
            }
        }
    }

    public static float progress(float partialTick) {
        return hatMenuProgressPrev + (hatMenuProgress - hatMenuProgressPrev) * partialTick;
    }
}
