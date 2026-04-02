package com.astryxion.astryxionshats.common.network;

import com.astryxion.astryxionshats.client.gui.toast.HatToast;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class PacketHatUnlocked {

    private final ItemStack hatStack;

    public PacketHatUnlocked(ItemStack hatStack) {
        this.hatStack = hatStack.copy();
    }

    public static void encode(PacketHatUnlocked msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.hatStack);
    }

    public static PacketHatUnlocked decode(FriendlyByteBuf buf) {
        return new PacketHatUnlocked(buf.readItem());
    }

    public static void handle(PacketHatUnlocked msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.getToasts().addToast(new HatToast(msg.hatStack));
    }
}
