package com.astryxion.astryxionshats.common.network;

import com.astryxion.astryxionshats.AstryxionsHats;
import net.minecraft.advancements.AdvancementHolder;
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
            AdvancementHolder holder = player.getServer().getAdvancements()
                    .get(ResourceLocation.fromNamespaceAndPath(AstryxionsHats.MODID, "hats/hunt_begin"));
            if (holder != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        player.getAdvancements().award(holder, criterion);
                    }
                }
            }
        }
    }
}
