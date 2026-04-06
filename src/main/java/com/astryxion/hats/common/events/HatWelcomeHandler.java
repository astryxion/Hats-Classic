package com.astryxion.hats.common.events;

import com.astryxion.hats.Hats;
import com.astryxion.hats.Config;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.HatMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hats.MODID)
public class HatWelcomeHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(HatDataCapability.HAT_DATA).ifPresent(data -> {
            // Only show the message if the player hasn't seen it yet in this world
            if (!data.hasSeenWelcome()) {

                // 🔧 LOGIC UPDATE:
                // Treat Creative players as "Cosmetic Mode" users regardless of config.
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

                // 🔧 Mark as seen so they never see it again in this world
                data.setSeenWelcome(true);
            }
        });
    }
}