package com.astryxion.hats.common.hat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * Stores per-player hat unlock data and the currently equipped hat.
 * Saved per-player, per-world.
 */
public interface PlayerHatData {

    /**
     * Unlock a hat for the player
     */
    void unlockHat(ResourceLocation hatId);

    /**
     * Check if a hat is unlocked
     */
    boolean hasHat(ResourceLocation hatId);

    /**
     * Get all unlocked hats
     */
    Set<ResourceLocation> getUnlockedHats();

    /**
     * 🔧 GET the currently equipped hat ID.
     * Returns null if no hat is equipped.
     */
    ResourceLocation getEquippedHat();

    /**
     * 🔧 SET the currently equipped hat ID.
     */
    void setEquippedHat(ResourceLocation hatId);

    /**
     * 🔧 Check if the player has already seen the initial mode message.
     */
    boolean hasSeenWelcome();

    /**
     * 🔧 Set whether the player has seen the initial mode message.
     */
    void setSeenWelcome(boolean seen);

    /**
     * Copy data from another instance.
     * REQUIRED for Forge capability cloning
     * (death, respawn, dimension change).
     */
    void copyFrom(PlayerHatData other);

    /**
     * Clear all data.
     * Used for new worlds, resets, or debug.
     */
    void clear();

    // ==========================================
    // NBT PERSISTENCE METHODS
    // These allow the data to be saved to disk
    // and synced over the network.
    // ==========================================

    /**
     * Converts the current data into NBT for saving or syncing.
     */
    CompoundTag serializeNBT();

    /**
     * Loads the data from NBT.
     */
    void deserializeNBT(CompoundTag nbt);
}