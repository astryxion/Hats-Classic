package com.astryxion.hats.common.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * World-specific hat inventory (iChun-style)
 * One per world save
 */
public class PlayerHatInventory extends SavedData {

    private static final String DATA_NAME = "hats_inventory";

    // ============================
    // Per player data
    // ============================

    private final Map<UUID, Set<Item>> unlockedHats = new HashMap<>();
    private final Map<UUID, Item> equippedHat = new HashMap<>();

    // ============================
    // Get instance
    // ============================

    public static PlayerHatInventory get(ServerLevel level) {

        return level.getDataStorage().computeIfAbsent(
                PlayerHatInventory::load,
                PlayerHatInventory::new,
                DATA_NAME
        );
    }

    // ============================
    // Unlock logic
    // ============================

    public void unlockHat(UUID player, Item hat) {

        if (hat == null || hat == Items.AIR) return;

        unlockedHats
                .computeIfAbsent(player, id -> new HashSet<>())
                .add(hat);

        setDirty();
    }

    public boolean hasHatUnlocked(UUID player, Item hat) {

        return unlockedHats
                .getOrDefault(player, Collections.emptySet())
                .contains(hat);
    }

    public Set<Item> getUnlockedHats(UUID player) {

        return unlockedHats
                .getOrDefault(player, Collections.emptySet());
    }

    // ============================
    // Equip logic
    // ============================

    public void equipHat(UUID player, Item hat) {

        if (hat == null || hat == Items.AIR) {
            equippedHat.remove(player);
        } else {
            equippedHat.put(player, hat);
        }

        setDirty();
    }

    public Item getEquippedHat(UUID player) {

        return equippedHat.getOrDefault(player, Items.AIR);
    }

    public void clearEquippedHat(UUID player) {

        equippedHat.remove(player);
        setDirty();
    }

    // ============================
    // Saving
    // ============================

    @Override
    public CompoundTag save(CompoundTag tag) {

        ListTag playersList = new ListTag();

        for (UUID uuid : unlockedHats.keySet()) {

            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", uuid);

            // Unlocked hats
            ListTag hatsTag = new ListTag();

            for (Item hat : unlockedHats.get(uuid)) {

                ResourceLocation id = BuiltInRegistries.ITEM.getKey(hat);
                if (id != null) {
                    hatsTag.add(StringTag.valueOf(id.toString()));
                }
            }

            playerTag.put("Unlocked", hatsTag);

            // Equipped hat
            Item equipped = equippedHat.get(uuid);

            if (equipped != null && equipped != Items.AIR) {

                ResourceLocation eqId = BuiltInRegistries.ITEM.getKey(equipped);
                if (eqId != null) {
                    playerTag.putString("Equipped", eqId.toString());
                }
            }

            playersList.add(playerTag);
        }

        tag.put("Players", playersList);

        return tag;
    }

    // ============================
    // Loading
    // ============================

    public static PlayerHatInventory load(CompoundTag tag) {

        PlayerHatInventory data = new PlayerHatInventory();

        ListTag playersList = tag.getList("Players", 10);

        for (int i = 0; i < playersList.size(); i++) {

            CompoundTag playerTag = playersList.getCompound(i);

            UUID uuid = playerTag.getUUID("Player");

            // Load unlocked
            Set<Item> hats = new HashSet<>();

            ListTag hatsTag = playerTag.getList("Unlocked", 8);

            for (int j = 0; j < hatsTag.size(); j++) {

                ResourceLocation id = new ResourceLocation(hatsTag.getString(j));
                Item item = BuiltInRegistries.ITEM.get(id);

                if (item != null) {
                    hats.add(item);
                }
            }

            data.unlockedHats.put(uuid, hats);

            // Load equipped
            if (playerTag.contains("Equipped")) {

                ResourceLocation id = new ResourceLocation(playerTag.getString("Equipped"));
                Item item = BuiltInRegistries.ITEM.get(id);

                if (item != null) {
                    data.equippedHat.put(uuid, item);
                }
            }
        }

        return data;
    }
}
