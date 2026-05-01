package com.astryxion.hats.common.hat;

import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Stores the specific hat instance for a mob.
 * Fixed to save the full ItemStack to prevent identity mismatches.
 */
public class HatPart {

    private ItemStack hat = ItemStack.EMPTY;

    public ItemStack getHatStack() {
        return hat;
    }

    public void setHat(Item item) {
        this.hat = (item == null) ? ItemStack.EMPTY : new ItemStack(item);
    }

    public void setHatStack(ItemStack stack) {
        this.hat = (stack == null) ? ItemStack.EMPTY : stack.copy();
    }

    public boolean hasHat() {
        return !hat.isEmpty();
    }

    public void clear() {
        hat = ItemStack.EMPTY;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (!hat.isEmpty()) {
            var ops = RegistryOps.create(NbtOps.INSTANCE, provider);
            DataResult<Tag> encoded = ItemStack.CODEC.encodeStart(ops, hat);
            tag.put("HatStack", encoded.getOrThrow());
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("HatStack")) {
            var ops = RegistryOps.create(NbtOps.INSTANCE, provider);
            DataResult<ItemStack> decoded = ItemStack.CODEC.parse(ops, tag.get("HatStack"));
            this.hat = decoded.result().orElse(ItemStack.EMPTY);
        } else {
            this.hat = ItemStack.EMPTY;
        }
    }
}
