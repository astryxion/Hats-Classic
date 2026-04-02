package com.astryxion.astryxionshats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;

/**
 * Handles hat positioning for zombies
 * (ported + simplified from iChun's Hats logic)
 */
public class ZombieHatHelper {

    /**
     * Apply zombie-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be zombie)
        if (!(entity instanceof Zombie))
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
        // SCALE (zombie head is bigger)
        // ============================

        float scale = 0.72f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // HEIGHT OFFSET (head position)
        // ============================

        poseStack.translate(0.0D, 0.38D, 0.0D);
    }
}
