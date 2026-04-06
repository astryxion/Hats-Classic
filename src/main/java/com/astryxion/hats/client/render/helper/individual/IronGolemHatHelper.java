package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;

/**
 * Handles hat positioning for iron golems
 * (ported + simplified from iChun's Hats logic)
 */
public class IronGolemHatHelper {

    /**
     * Apply iron-golem-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be iron golem)
        if (!(entity instanceof IronGolem))
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
        // SCALE (massive head)
        // ============================

        float scale = 0.85f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // down a LOT + in a bit
        // ============================

        poseStack.translate(
                0.0D,   // X
                0.50D,  // Y (very low head position)
                -0.08D   // Z (thick brow)
        );
    }
}
