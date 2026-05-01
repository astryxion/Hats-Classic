package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Husk;

/**
 * Handles hat positioning for husks
 * (ported + simplified from iChun's Hats logic)
 */
public class HuskHatHelper {

    /**
     * Apply husk-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be husk)
        if (!(entity instanceof Husk))
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
        // SCALE (same skull, taller body)
        // ============================

        float scale = 0.71f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // HEIGHT OFFSET (desert posture)
        // ============================

        poseStack.translate(0.0D, 0.44D, 0.0D);
    }
}
