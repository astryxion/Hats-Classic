package com.astryxion.hats.common.hat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import java.util.Set;

/**
 * Stores per-player hat unlock data and the currently equipped hat.
 * Saved per-player, per-world.
 */
public interface PlayerHatData {

    /**
     * Unlock a hat for the player
     */
    void unlockHat(Identifier hatId);

    /**
     * Check if a hat is unlocked
     */
    boolean hasHat(Identifier hatId);

    /**
     * Get all unlocked hats
     */
    Set<Identifier> getUnlockedHats();

    /**
     * 🔧 GET the currently equipped hat ID.
     * Returns null if no hat is equipped.
     */
    Identifier getEquippedHat();

    /**
     * 🔧 SET the currently equipped hat ID.
     */
    void setEquippedHat(Identifier hatId);

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