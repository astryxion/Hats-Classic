package com.astryxion.hats.client.trade;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientTradeFeedback {

    private ClientTradeFeedback() {}

    public static void onResolved(boolean accepted, String otherName) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        Component message = accepted
                ? Component.translatable("hats.trade.accepted", otherName).withStyle(ChatFormatting.GREEN)
                : Component.translatable("hats.trade.declined", otherName).withStyle(ChatFormatting.RED);
        minecraft.player.sendOverlayMessage(message);
        if (accepted) {
            com.astryxion.hats.client.gui.HatsGui.open();
        }
    }
}
