package com.astryxion.astryxionshats.common.hat;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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

    // ============================================================
    // THE FIX: Save/Load the FULL STACK to sync Server and Client
    // ============================================================

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (!hat.isEmpty()) {
            tag.put("HatStack", hat.save(provider));
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("HatStack")) {
            ItemStack parsed = ItemStack.parseOptional(provider, tag.getCompound("HatStack"));
            this.hat = parsed != null ? parsed : ItemStack.EMPTY;
        } else {
            this.hat = ItemStack.EMPTY;
        }
    }
}