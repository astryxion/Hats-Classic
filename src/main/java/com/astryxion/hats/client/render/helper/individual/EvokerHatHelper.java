package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.Evoker;

/**
 * Handles hat positioning for evokers
 * (ported + simplified from iChun's Hats logic)
 */
public class EvokerHatHelper {

    /**
     * Apply evoker-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be evoker)
        if (!(entity instanceof Evoker))
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
        // SCALE (illager head, refined)
        // ============================

        float scale = 0.70f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // slightly higher than vindicator
        // ============================

        poseStack.translate(
                0.0D,   // X
                0.45D,  // Y (most upright illager)
                -0.03D   // Z (same inward pull)
        );
    }
}
