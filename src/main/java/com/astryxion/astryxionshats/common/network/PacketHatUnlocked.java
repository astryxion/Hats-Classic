package com.astryxion.astryxionshats.common.network;

import com.astryxion.astryxionshats.client.gui.toast.HatToast;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class PacketHatUnlocked {

    private final ItemStack hatStack;

    public PacketHatUnlocked(ItemStack hatStack) {
        this.hatStack = hatStack.copy();
    }

    public static void encode(PacketHatUnlocked msg, FriendlyByteBuf buf, HolderLookup.Provider registryAccess) {
        buf.writeNbt(msg.hatStack.save(registryAccess));
    }

    public static PacketHatUnlocked decode(FriendlyByteBuf buf, HolderLookup.Provider registryAccess) {
        CompoundTag tag = buf.readNbt();
        ItemStack stack = tag != null ? ItemStack.parseOptional(registryAccess, tag) : null;
        return new PacketHatUnlocked(stack != null ? stack : ItemStack.EMPTY);
    }

    public static void handle(PacketHatUnlocked msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.getToasts().addToast(new HatToast(msg.hatStack));
    }
}
