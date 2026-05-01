package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.squid.Squid;

/**
 * Handles hat positioning for squids
 * (ported + simplified from iChun's Hats logic)
 */
public class SquidHatHelper {

    /**
     * Apply squid-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be squid)
        if (!(entity instanceof Squid))
            return;

        // ============================
        // FLIP (model orientation)
        // ============================

        poseStack.mulPose(Axis.XP.rotationDegrees(180f));

        // ============================
        // ROTATE (face forward)
        // ============================

        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // ============================
        // SCALE (squid head is wide)
        // ============================

        float scale = 0.90f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // centered on mantle
        // ============================

        poseStack.translate(
                0.0D,   // X
                -0.30D,  // Y (top of mantle)
                -0.02D   // Z (slight inward)
        );
    }
}
