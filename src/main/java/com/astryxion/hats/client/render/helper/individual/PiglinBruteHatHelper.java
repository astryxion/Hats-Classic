package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;

/**
 * Handles hat positioning for piglin brutes
 * (ported + simplified from iChun's Hats logic)
 */
public class PiglinBruteHatHelper {

    /**
     * Apply piglin-brute-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be piglin brute)
        if (!(entity instanceof PiglinBrute))
            return;

        // ============================
        // FLIP (fix upside down)
        // ============================

        poseStack.mulPose(Axis.XP.rotationDegrees(180f));

        // ============================
        // ROTATE (fix backwards)
        // ============================

        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // ============================
        // SCALE (bigger brute head)
        // ============================

        float scale = 0.76f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // HEIGHT OFFSET (thicker skull)
        // ============================

        poseStack.translate(0.0D, 0.46D, 0.0D);
    }
}
