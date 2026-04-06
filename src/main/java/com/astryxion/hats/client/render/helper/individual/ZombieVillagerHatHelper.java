package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;

/**
 * Handles hat positioning for zombie villagers
 * (ported + simplified from iChun's Hats logic)
 */
public class ZombieVillagerHatHelper {

    /**
     * Apply zombie-villager-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be zombie villager)
        if (!(entity instanceof ZombieVillager))
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
        // SCALE (villager-style wide head)
        // ============================

        float scale = 0.70f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // zombie posture + villager nose
        // ============================

        poseStack.translate(
                0.0D,   // X
                0.42D,  // Y (slightly lower than villager)
                -0.05D   // Z (pull back for nose)
        );
    }
}
