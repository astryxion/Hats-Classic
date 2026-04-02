package com.astryxion.astryxionshats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;

/**
 * Handles hat positioning for endermen
 * (ported + simplified from iChun's Hats logic)
 */
public class EndermanHatHelper {

    /**
     * Apply enderman-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be enderman)
        if (!(entity instanceof EnderMan))
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
        // SCALE (tall, narrow head)
        // ============================

        float scale = 0.78f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // lower + slightly inward
        // ============================

        poseStack.translate(
                0.0D,   // X
                0.30D,  // Y (tall neck)
                -0.04D   // Z (pull into skull)
        );
    }
}
