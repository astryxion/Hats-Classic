package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;

/**
 * Handles hat positioning for piglins
 * (ported + simplified from iChun's Hats logic)
 */
public class PiglinHatHelper {

    /**
     * Apply piglin-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be piglin)
        if (!(entity instanceof Piglin))
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
        // SCALE (piglin head is wider)
        // ============================

        float scale = 0.70f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // HEIGHT OFFSET (snout + ears)
        // ============================

        poseStack.translate(0.0D, 0.44D, 0.0D);
    }
}
