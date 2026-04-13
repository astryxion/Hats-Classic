package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.Pillager;

/**
 * Handles hat positioning for pillagers
 * (ported + simplified from iChun's Hats logic)
 */
public class PillagerHatHelper {

    /**
     * Apply pillager-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be pillager)
        if (!(entity instanceof Pillager))
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
        // SCALE (wide illager head)
        // ============================

        float scale = 0.70f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // down a bit + in a bit
        // ============================

        poseStack.translate(
                0.0D,   // X
                0.43D,  // Y (lower on forehead)
                -0.03D   // Z (pull inward)
        );
    }
}
