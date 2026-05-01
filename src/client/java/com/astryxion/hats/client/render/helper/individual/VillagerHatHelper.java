package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;

/**
 * Handles hat positioning for villagers
 * (ported + simplified from iChun's Hats logic)
 */
public class VillagerHatHelper {

    /**
     * Apply villager-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be villager)
        if (!(entity instanceof Villager))
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
        // SCALE (wide villager head)
        // ============================

        float scale = 0.70f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // up slightly + back for nose
        // ============================

        poseStack.translate(
                0.0D,   // X
                0.44D,  // Y (forehead height)
                -0.05D   // Z (pull back to clear nose)
        );
    }
}
