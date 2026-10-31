package com.astryxion.hats.common.hat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!hat.isEmpty()) {
            ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, hat).result().ifPresent(saved -> tag.put("HatStack", saved));
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("HatStack")) {
            this.hat = ItemStack.CODEC.parse(NbtOps.INSTANCE, tag.getCompoundOrEmpty("HatStack")).result().orElse(ItemStack.EMPTY);
        } else {
            this.hat = ItemStack.EMPTY;
        }
    }
}