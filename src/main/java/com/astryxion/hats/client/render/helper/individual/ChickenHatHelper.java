package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;

public class ChickenHatHelper {

    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        if (!(entity instanceof Chicken))
            return;

        // Fix vanilla item orientation
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // Chickens have tiny heads
        float scale = 0.30f;
        poseStack.scale(scale, scale, scale);

        // Offsets (tuned similar to iChun style)

        double yOffset = 1.00;   // slightly down
        double zOffset = -0.06;   // slightly forward

        poseStack.translate(0.0, yOffset, zOffset);
    }
}
