package com.astryxion.hats.common.hat;

import com.astryxion.hats.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PlayerHatDataImpl implements PlayerHatData {

    private final Set<Identifier> unlockedHats = new HashSet<>();
    private Identifier equippedHat = null;
    private boolean seenWelcome = false; // 🔧 NEW: Track if they've seen the message

    @Override
    public void unlockHat(Identifier hatId) {
        if (Config.hatMode != HatMode.COSMETIC) {
            unlockedHats.add(hatId);
        }
    }

    @Override
    public void grantHat(Identifier hatId) {
        if (hatId != null) {
            unlockedHats.add(hatId);
        }
    }

    @Override
    public void revokeHat(Identifier hatId) {
        if (hatId != null) {
            unlockedHats.remove(hatId);
            if (hatId.equals(equippedHat)) {
                equippedHat = null;
            }
        }
    }

    @Override
    public boolean hasHat(Identifier hatId) {
        if (Config.hatMode == HatMode.COSMETIC) {
            return true;
        }
        return unlockedHats.contains(hatId);
    }

    @Override
    public Set<Identifier> getUnlockedHats() {
        return Collections.unmodifiableSet(unlockedHats);
    }

    @Override
    public Identifier getEquippedHat() {
        return equippedHat;
    }

    @Override
    public void setEquippedHat(Identifier hatId) {
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

        for (Identifier hatId : unlockedHats) {
            list.add(list.size(), StringTag.valueOf(hatId.toString()));
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
        ListTag list = nbt.getListOrEmpty("UnlockedHats");
        for (int i = 0; i < list.size(); i++) {
            String raw = list.getStringOr(i, "");
            if (!raw.isEmpty()) {
                unlockedHats.add(Identifier.parse(raw));
            }
        }

        String equipped = nbt.getStringOr("EquippedHat", "");
        if (!equipped.isEmpty()) {
            this.equippedHat = Identifier.parse(equipped);
        } else {
            this.equippedHat = null;
        }

        this.seenWelcome = nbt.getBooleanOr("SeenWelcome", false);
    }
}