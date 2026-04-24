package com.astryxion.hats.client.network;

import com.astryxion.hats.client.gui.toast.HatToast;
import com.astryxion.hats.common.network.PacketHatUnlocked;
import net.minecraft.client.Minecraft;

public final class PacketHatUnlockedClient {
    private PacketHatUnlockedClient() {}

    public static void handle(PacketHatUnlocked msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.getToasts().addToast(new HatToast(msg.getHatStack()));
    }
}
