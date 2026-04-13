package com.astryxion.hats.client.render.helper.individual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Handles hat positioning for players
 */
public class PlayerHatHelper {

    /**
     * Apply player-specific hat transforms
     */
    public static void apply(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // Safety (should always be player)
        if (!(entity instanceof Player player))
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
        // HELMET CHECK & SCALE/POSITIONING
        // ============================

        float scale = 0.62f; // Default small scale
        double yOffset = 0.42D; // Default height

        // Check if the player is wearing a helmet
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if (!helmet.isEmpty()) {
            // DEBUG: Make the hat massive (Scale 1.0) when wearing a helmet
            scale = 0.70f;

            // Adjust height to account for the massive size and helmet clearance
            yOffset = 0.42D;
        }

        // Apply the chosen scale
        poseStack.scale(scale, scale, scale);

        // Apply final translation
        poseStack.translate(0.0D, yOffset, 0.0D);
    }
}