package com.astryxion.astryxionshats.common.events;

import com.astryxion.astryxionshats.Config;
import com.astryxion.astryxionshats.common.capability.HatDataCapability;
import com.astryxion.astryxionshats.common.hat.HatMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class HatWelcomeHandler {

    public static void onPlayerJoin(ServerPlayer player) {
        HatDataCapability.get(player).ifPresent(data -> {
            if (!data.hasSeenWelcome()) {

                boolean showCosmeticMessage = player.isCreative() || Config.hatMode == HatMode.COSMETIC;

                if (showCosmeticMessage) {
                    player.sendSystemMessage(Component.literal("Welcome to ")
                            .append(Component.literal("Cosmetic Mode").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                            .append("! All hats are unlocked. ")
                            .append(Component.literal("Press H").withStyle(ChatFormatting.YELLOW))
                            .append(" to see your unlocked hats."));
                } else {
                    player.sendSystemMessage(Component.literal("Welcome to ")
                            .append(Component.literal("Hat Hunt Mode").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                            .append("! Kill animals wearing hats to unlock them. ")
                            .append(Component.literal("Press H").withStyle(ChatFormatting.YELLOW))
                            .append(" to see your unlocked hats."));
                }

                data.setSeenWelcome(true);
                com.astryxion.astryxionshats.common.capability.HatDataCapability.markDirty(player);
            }
        });
    }
}
