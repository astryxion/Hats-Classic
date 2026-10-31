package com.astryxion.hats.client.tabula;

import com.astryxion.hats.mixin.ModelPartAccessor;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;

/**
 * Exact Hats Renewed {@code LayerHat} crown-anchor for Tabula models:
 * XZ center and minY of the primary head cube, scaled by width/8, plus the
 * 1.005 z-fight inflate around the 4px head half.
 */
public final class TabulaHatPlacement {

    public static final float Z_FIGHT_INFLATE = 1.005f;
    public static final float HEAD_HALF = 4.0f / 16.0f;

    private static final float PLAYER_HEAD_UNITS = 8.0f;
    private static final float MIN_HEAD_CUBE_VOL = 32f;

    private TabulaHatPlacement() {}

    public static float applyGeometryOffset(PoseStack poseStack, ModelPart head) {
        float[] off = computeHeadOffsets(head);
        poseStack.translate(
                Float.isNaN(off[0]) ? 0f : off[0],
                Float.isNaN(off[1]) ? -8f / 16f : off[1],
                Float.isNaN(off[2]) ? 0f : off[2]
        );
        return Float.isNaN(off[3]) ? 1f : off[3];
    }

    public static void applyZFight(PoseStack poseStack) {
        poseStack.translate(0.0, HEAD_HALF, 0.0);
        poseStack.scale(Z_FIGHT_INFLATE, Z_FIGHT_INFLATE, Z_FIGHT_INFLATE);
        poseStack.translate(0.0, -HEAD_HALF, 0.0);
    }

    private static float[] computeHeadOffsets(ModelPart head) {
        float[] nan = {Float.NaN, Float.NaN, Float.NaN, Float.NaN};
        if (head == null) {
            return nan;
        }

        List<ModelPart.Cube> cubes = cubesOf(head);
        if (!cubes.isEmpty()) {
            return offsetsFromCubes(cubes, 0f, 0f, 0f);
        }

        Map<String, ModelPart> children = childrenOf(head);
        if (children.isEmpty()) {
            return nan;
        }

        ModelPart bestChild = null;
        float bestVol = 0f;
        for (ModelPart child : children.values()) {
            float vol = totalVolume(child);
            if (vol > bestVol) {
                bestVol = vol;
                bestChild = child;
            }
        }

        if (bestChild != null) {
            return offsetsFromCubes(cubesOf(bestChild),
                    bestChild.x / 16f, bestChild.y / 16f, bestChild.z / 16f);
        }
        return nan;
    }

    private static float[] offsetsFromCubes(List<ModelPart.Cube> cubes, float px, float py, float pz) {
        ModelPart.Cube primary = null;
        for (ModelPart.Cube c : cubes) {
            if (volume(c) < MIN_HEAD_CUBE_VOL) {
                continue;
            }
            if (primary == null || c.minY < primary.minY
                    || (c.minY == primary.minY && volume(c) > volume(primary))) {
                primary = c;
            }
        }
        if (primary != null) {
            float headWidthUnits = primary.maxX - primary.minX;
            float scale = Mth.clamp(headWidthUnits / PLAYER_HEAD_UNITS, 0.2f, 3.0f);
            return new float[]{
                    (primary.minX + primary.maxX) / 2f / 16f + px,
                    primary.minY / 16f + py,
                    (primary.minZ + primary.maxZ) / 2f / 16f + pz,
                    scale
            };
        }

        if (cubes.isEmpty()) {
            return new float[]{Float.NaN, Float.NaN, Float.NaN, Float.NaN};
        }

        float topY = Float.MAX_VALUE;
        for (ModelPart.Cube c : cubes) {
            topY = Math.min(topY, c.minY);
        }

        float sumX = 0, sumZ = 0, minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        int count = 0;
        for (ModelPart.Cube c : cubes) {
            if (c.minY > topY + 2f) {
                continue;
            }
            sumX += (c.minX + c.maxX) / 2f;
            sumZ += (c.minZ + c.maxZ) / 2f;
            minX = Math.min(minX, c.minX);
            maxX = Math.max(maxX, c.maxX);
            count++;
        }
        float cx = count > 0 ? sumX / count : 0f;
        float cz = count > 0 ? sumZ / count : 0f;
        float span = maxX > minX ? (maxX - minX) : PLAYER_HEAD_UNITS;
        float scale = Mth.clamp(span / PLAYER_HEAD_UNITS, 0.2f, 3.0f);
        return new float[]{
                cx / 16f + px,
                topY / 16f + py,
                cz / 16f + pz,
                scale
        };
    }

    private static float totalVolume(ModelPart part) {
        float vol = 0f;
        for (ModelPart.Cube cube : cubesOf(part)) {
            vol += volume(cube);
        }
        for (ModelPart child : childrenOf(part).values()) {
            vol += totalVolume(child);
        }
        return vol;
    }

    private static float volume(ModelPart.Cube c) {
        return (c.maxX - c.minX) * (c.maxY - c.minY) * (c.maxZ - c.minZ);
    }

    private static List<ModelPart.Cube> cubesOf(ModelPart part) {
        return ((ModelPartAccessor) (Object) part).hats$cubes();
    }

    private static Map<String, ModelPart> childrenOf(ModelPart part) {
        return ((ModelPartAccessor) (Object) part).hats$children();
    }
}
