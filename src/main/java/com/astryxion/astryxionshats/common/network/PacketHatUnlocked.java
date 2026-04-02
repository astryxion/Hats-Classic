package com.astryxion.astryxionshats.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Server → Client packet (payload only). Client-side handling lives in
 * {@link com.astryxion.astryxionshats.client.network.HatClientPacketHandlers} so dedicated servers never load GUI classes.
 */
public class PacketHatUnlocked {

    private final ItemStack hatStack;

    public PacketHatUnlocked(ItemStack hatStack) {
        this.hatStack = hatStack.copy();
    }

    public ItemStack getHatStack() {
        return hatStack;
    }

    public static void encode(PacketHatUnlocked msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.hatStack);
    }

    public static PacketHatUnlocked decode(FriendlyByteBuf buf) {
        return new PacketHatUnlocked(buf.readItem());
    }
}
