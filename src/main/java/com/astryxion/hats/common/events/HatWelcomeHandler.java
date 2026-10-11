package com.astryxion.hats.common.events;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;
import com.astryxion.hats.Config;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.hat.PlayerHatData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = Hats.MODID)
public class HatWelcomeHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!Config.showWelcomeMessage) return;

        PlayerHatData data = player.getData(HatDataCapability.HAT_DATA);
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
        }
    }
}