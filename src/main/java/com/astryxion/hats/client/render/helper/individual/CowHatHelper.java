package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.LivingEntity;

public class CowHatHelper {

    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {
        // Flip item orientation (same as player)
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // Scale down slightly (cow heads are wider)
        float scale = 0.60f;
        poseStack.scale(scale, scale, scale);

        // === iChun-derived positioning ===
        // Forward onto snout
        poseStack.translate(0.0D, 0.0D, -0.35D);

        // Lift onto head
        poseStack.translate(0.0D, 0.10D, 0.0D);
    }
}
