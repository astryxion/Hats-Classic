package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles hat positioning for strays
 * (ported + simplified from iChun's Hats logic)
 */
public class StrayHatHelper {

    /**
     * Apply stray-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be stray)
        if (entity.getType() != EntityType.STRAY)
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
        // SCALE (slimmer than skeleton)
        // ============================

        float scale = 0.66f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // HEIGHT OFFSET (hood + posture)
        // ============================

        poseStack.translate(0.0D, 0.42D, 0.0D);
    }
}
