package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;

public class SheepHatHelper {

    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        if (!(entity instanceof Sheep))
            return;

        // Fix vanilla item orientation
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // Scale (sheep smaller head than cow)
        float scale = 0.6f;
        poseStack.scale(scale, scale, scale);

        // Offsets (tuned from iChun hats)
        double yOffset = 0.09;
        double zOffset = -0.15;

        poseStack.translate(0.0, yOffset, zOffset);
    }
}
