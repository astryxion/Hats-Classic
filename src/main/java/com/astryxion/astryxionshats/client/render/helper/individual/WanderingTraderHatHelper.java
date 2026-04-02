package com.astryxion.astryxionshats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.WanderingTrader;

/**
 * Handles hat positioning for wandering traders
 * (ported + simplified from iChun's Hats logic)
 */
public class WanderingTraderHatHelper {

    /**
     * Apply wandering-trader-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be wandering trader)
        if (!(entity instanceof WanderingTrader))
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
        // SCALE (same as villager)
        // ============================

        float scale = 0.70f;
        poseStack.scale(scale, scale, scale);

        // ============================
        // POSITION OFFSET
        // slight lift to clear hood + nose
        // ============================

        poseStack.translate(
                0.0D,   // X
                0.46D,  // Y (slightly higher than villager)
                -0.05D   // Z (pull back for nose/hood)
        );
    }
}
