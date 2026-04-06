package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Handles hat positioning for creepers
 * (ported + simplified from iChun's Hats logic)
 */
public class CreeperHatHelper {

    /**
     * Apply creeper-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be creeper)
        if (!(entity instanceof Creeper))
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
        // SCALE (tall, flat head)
        // ============================

        float scale = 0.72f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // down a bit + slightly inward
        // ============================

        poseStack.translate(
                0.0D,   // X
                0.31D,  // Y (creeper head height)
                0.00D   // Z (very slight inward)
        );
    }
}
