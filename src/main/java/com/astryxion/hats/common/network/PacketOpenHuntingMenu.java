package com.astryxion.hats.common.network;

import com.astryxion.hats.AstryxionsHats;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PacketOpenHuntingMenu {

    public PacketOpenHuntingMenu() {}
    public PacketOpenHuntingMenu(FriendlyByteBuf buffer) {}
    public void toBytes(FriendlyByteBuf buffer) {}

    public static void handle(PacketOpenHuntingMenu msg, ServerPlayer player) {
        if (player != null && !player.isCreative()) {
            Advancement advancement = player.getServer().getAdvancements()
                    .getAdvancement(new ResourceLocation(AstryxionsHats.MODID, "hats/hunt_begin"));
            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        player.getAdvancements().award(advancement, criterion);
                    }
                }
            }
        }
    }
}
