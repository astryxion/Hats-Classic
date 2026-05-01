package com.astryxion.hats.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PacketOpenCosmeticMenu {

    public PacketOpenCosmeticMenu() {}

    public PacketOpenCosmeticMenu(FriendlyByteBuf buffer) {}

    public void toBytes(FriendlyByteBuf buffer) {}

    public static void handle(PacketOpenCosmeticMenu msg, ServerPlayer player) {
        if (player != null && !player.isCreative()) {
            // Menu opens purely for cosmetic purposes
        }
    }
}
