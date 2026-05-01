package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;

/**
 * Handles hat positioning for skeletons
 * (ported + simplified from iChun's Hats logic)
 */
public class SkeletonHatHelper {

    /**
     * Apply skeleton-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be skeleton)
        if (!(entity instanceof Skeleton))
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
        // SCALE (slightly smaller skull)
        // ============================

        float scale = 0.68f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // HEIGHT OFFSET (skull height)
        // ============================

        poseStack.translate(0.0D, 0.40D, 0.0D);
    }
}
