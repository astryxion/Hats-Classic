package com.astryxion.hats.common.network;

import com.astryxion.hats.client.gui.toast.HatToast;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class PacketHatUnlocked {

    private final ItemStack hatStack;

    public PacketHatUnlocked(ItemStack hatStack) {
        this.hatStack = hatStack.copy();
    }

    public static void encode(PacketHatUnlocked msg, FriendlyByteBuf buf, HolderLookup.Provider registryAccess) {
        var ops = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
        DataResult<Tag> encoded = ItemStack.CODEC.encodeStart(ops, msg.hatStack);
        buf.writeNbt((CompoundTag) encoded.getOrThrow());
    }

    public static PacketHatUnlocked decode(FriendlyByteBuf buf, HolderLookup.Provider registryAccess) {
        CompoundTag tag = buf.readNbt();
        if (tag == null) {
            return new PacketHatUnlocked(ItemStack.EMPTY);
        }
        var ops = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
        DataResult<ItemStack> decoded = ItemStack.CODEC.parse(ops, tag);
        return new PacketHatUnlocked(decoded.result().orElse(ItemStack.EMPTY));
    }

    public static void handle(PacketHatUnlocked msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.getToastManager().addToast(new HatToast(msg.hatStack));
    }
}
