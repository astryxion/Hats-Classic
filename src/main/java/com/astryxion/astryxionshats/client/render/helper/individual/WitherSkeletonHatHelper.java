package com.astryxion.astryxionshats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;

/**
 * Handles hat positioning for wither skeletons
 * (ported + simplified from iChun's Hats logic)
 */
public class WitherSkeletonHatHelper {

    /**
     * Apply wither-skeleton-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be wither skeleton)
        if (!(entity instanceof WitherSkeleton))
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
        // SCALE (bigger skull)
        // ============================

        float scale = 0.74f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // down a bit + in a bit
        // ============================

        poseStack.translate(
                0.0D,   // X (centered)
                0.25D,  // Y (lower than before)
                -0.03D   // Z (pull inward)
        );
    }
}
