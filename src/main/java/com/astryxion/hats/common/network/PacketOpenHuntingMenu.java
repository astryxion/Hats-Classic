package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class PacketOpenHuntingMenu {

    public PacketOpenHuntingMenu() {}
    public PacketOpenHuntingMenu(FriendlyByteBuf buffer) {}
    public void toBytes(FriendlyByteBuf buffer) {}

    public static void handle(PacketOpenHuntingMenu msg, ServerPlayer player) {
        if (player != null && !player.isCreative()) {
            AdvancementHolder holder = player.level().getServer().getAdvancements()
                    .get(Identifier.fromNamespaceAndPath(Hats.MODID, "hats/hunt_begin"));
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
