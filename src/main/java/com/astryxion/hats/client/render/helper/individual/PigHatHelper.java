package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;

public class PigHatHelper {

    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        if (!(entity instanceof Pig))
            return;

        // Fix vanilla item orientation
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // Pig head slightly bigger than sheep
        float scale = 0.7f;
        poseStack.scale(scale, scale, scale);

        // Tuned offsets (based on old Hats + modern testing)
        double yOffset = 0.0;
        double zOffset = -0.30; // pigs snout is longer

        poseStack.translate(0.0, yOffset, zOffset);
    }
}
