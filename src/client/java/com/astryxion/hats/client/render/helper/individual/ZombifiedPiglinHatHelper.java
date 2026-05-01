package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;

/**
 * Handles hat positioning for zombified piglins
 * (ported + simplified from iChun's Hats logic)
 */
public class ZombifiedPiglinHatHelper {

    /**
     * Apply zombified-piglin-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be zombified piglin)
        if (!(entity instanceof ZombifiedPiglin))
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
        // SCALE (slightly slimmer head)
        // ============================

        float scale = 0.69f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // HEIGHT OFFSET (taller posture)
        // ============================

        poseStack.translate(0.0D, 0.45D, 0.0D);
    }
}
