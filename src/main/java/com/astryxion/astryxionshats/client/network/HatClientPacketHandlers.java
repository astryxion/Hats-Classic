package com.astryxion.astryxionshats.client.network;

import com.astryxion.astryxionshats.client.gui.toast.HatToast;
import com.astryxion.astryxionshats.common.network.PacketHatUnlocked;
import net.minecraft.client.Minecraft;

public final class HatClientPacketHandlers {

    private HatClientPacketHandlers() {}

    public static void handleHatUnlocked(PacketHatUnlocked msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.getToasts().addToast(new HatToast(msg.getHatStack()));
    }
}
