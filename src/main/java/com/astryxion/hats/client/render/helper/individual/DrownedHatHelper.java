package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Drowned;

/**
 * Handles hat positioning for drowned
 * (ported + simplified from iChun's Hats logic)
 */
public class DrownedHatHelper {

    /**
     * Apply drowned-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be drowned)
        if (!(entity instanceof Drowned))
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
        // SCALE (same skull, hunched body)
        // ============================

        float scale = 0.70f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // HEIGHT OFFSET (lower head)
        // ============================

        poseStack.translate(0.0D, 0.39D, 0.0D);
    }
}
