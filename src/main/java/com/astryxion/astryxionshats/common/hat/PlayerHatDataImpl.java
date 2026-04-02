package com.astryxion.astryxionshats.common.hat;

import com.astryxion.astryxionshats.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PlayerHatDataImpl implements PlayerHatData {

    private final Set<ResourceLocation> unlockedHats = new HashSet<>();
    private ResourceLocation equippedHat = null;
    private boolean seenWelcome = false; // 🔧 NEW: Track if they've seen the message

    @Override
    public void unlockHat(ResourceLocation hatId) {
        if (Config.hatMode != HatMode.COSMETIC) {
            unlockedHats.add(hatId);
        }
    }

    @Override
    public boolean hasHat(ResourceLocation hatId) {
        if (Config.hatMode == HatMode.COSMETIC) {
            return true;
        }
        return unlockedHats.contains(hatId);
    }

    @Override
    public Set<ResourceLocation> getUnlockedHats() {
        return Collections.unmodifiableSet(unlockedHats);
    }

    @Override
    public ResourceLocation getEquippedHat() {
        return equippedHat;
    }

    @Override
    public void setEquippedHat(ResourceLocation hatId) {
        this.equippedHat = hatId;
    }

    @Override
    public boolean hasSeenWelcome() {
        return seenWelcome;
    }

    @Override
    public void setSeenWelcome(boolean seen) {
        this.seenWelcome = seen;
    }

    @Override
    public void clear() {
        unlockedHats.clear();
        this.equippedHat = null;
        this.seenWelcome = false;
    }

    @Override
    public void copyFrom(PlayerHatData other) {
        if (other == this) return;
        this.unlockedHats.clear();
        this.unlockedHats.addAll(other.getUnlockedHats());
        this.equippedHat = other.getEquippedHat();
        // 🔧 CRITICAL: Copy the welcome flag so they don't see it again after dying
        this.seenWelcome = other.hasSeenWelcome();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();

        for (ResourceLocation hatId : unlockedHats) {
            list.add(StringTag.valueOf(hatId.toString()));
        }
        nbt.put("UnlockedHats", list);

        if (equippedHat != null) {
            nbt.putString("EquippedHat", equippedHat.toString());
        }

        nbt.putBoolean("SeenWelcome", seenWelcome); // 🔧 Save to disk

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        unlockedHats.clear();
        if (nbt.contains("UnlockedHats", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("UnlockedHats", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                unlockedHats.add(new ResourceLocation(list.getString(i)));
            }
        }

        if (nbt.contains("EquippedHat", Tag.TAG_STRING)) {
            this.equippedHat = new ResourceLocation(nbt.getString("EquippedHat"));
        } else {
            this.equippedHat = null;
        }

        // 🔧 Load from disk (defaults to false if tag doesn't exist)
        this.seenWelcome = nbt.getBoolean("SeenWelcome");
    }
}