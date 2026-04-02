package com.astryxion.astryxionshats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vindicator;

/**
 * Handles hat positioning for vindicators
 * (ported + simplified from iChun's Hats logic)
 */
public class VindicatorHatHelper {

    /**
     * Apply vindicator-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be vindicator)
        if (!(entity instanceof Vindicator))
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
        // SCALE (same illager head)
        // ============================

        float scale = 0.70f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // slightly higher than pillager
        // ============================

        poseStack.translate(
                0.0D,   // X
                0.44D,  // Y (a touch higher than pillager)
                -0.03D   // Z (same pull inward)
        );
    }
}
