package com.astryxion.hats.common.data;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

/**
 * World-specific hat inventory (iChun-style)
 * One per world save
 */
public class PlayerHatInventory extends SavedData {

    private static final String DATA_NAME = "hats_inventory";

    public static final Codec<PlayerHatInventory> CODEC = CompoundTag.CODEC.xmap(
            PlayerHatInventory::load,
            data -> data.save(new CompoundTag())
    );

    public static final SavedDataType<PlayerHatInventory> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("hats", DATA_NAME),
            PlayerHatInventory::new,
            CODEC
    );

    // ============================
    // Per player data
    // ============================

    private final Map<UUID, Set<Item>> unlockedHats = new HashMap<>();
    private final Map<UUID, Item> equippedHat = new HashMap<>();

    // ============================
    // Get instance
    // ============================

    public static PlayerHatInventory get(ServerLevel level) {

        return level.getDataStorage().computeIfAbsent(TYPE);
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

    public CompoundTag save(CompoundTag tag) {

        ListTag playersList = new ListTag();

        for (UUID uuid : unlockedHats.keySet()) {

            CompoundTag playerTag = new CompoundTag();
            playerTag.putIntArray("Player", UUIDUtil.uuidToIntArray(uuid));

            ListTag hatsTag = new ListTag();

            for (Item hat : unlockedHats.get(uuid)) {

                Identifier id = BuiltInRegistries.ITEM.getKey(hat);
                if (id != null) {
                    hatsTag.add(hatsTag.size(), StringTag.valueOf(id.toString()));
                }
            }

            playerTag.put("Unlocked", hatsTag);

            Item equipped = equippedHat.get(uuid);

            if (equipped != null && equipped != Items.AIR) {

                Identifier eqId = BuiltInRegistries.ITEM.getKey(equipped);
                if (eqId != null) {
                    playerTag.putString("Equipped", eqId.toString());
                }
            }

            playersList.add(playersList.size(), playerTag);
        }

        tag.put("Players", playersList);

        return tag;
    }

    // ============================
    // Loading
    // ============================

    public static PlayerHatInventory load(CompoundTag tag) {

        PlayerHatInventory data = new PlayerHatInventory();

        ListTag playersList = tag.getListOrEmpty("Players");

        for (int i = 0; i < playersList.size(); i++) {

            CompoundTag playerTag = playersList.getCompoundOrEmpty(i);
            int[] rawUuid = playerTag.getIntArray("Player").orElse(null);
            if (rawUuid == null || rawUuid.length != 4) {
                continue;
            }
            UUID uuid = UUIDUtil.uuidFromIntArray(rawUuid);

            Set<Item> hats = new HashSet<>();

            ListTag hatsTag = playerTag.getListOrEmpty("Unlocked");

            for (int j = 0; j < hatsTag.size(); j++) {

                String idRaw = hatsTag.getStringOr(j, "");
                if (idRaw.isEmpty()) {
                    continue;
                }
                Identifier id = Identifier.parse(idRaw);
                Item item = BuiltInRegistries.ITEM.getValue(id);

                if (item != null) {
                    hats.add(item);
                }
            }

            data.unlockedHats.put(uuid, hats);

            String equipped = playerTag.getStringOr("Equipped", "");
            if (!equipped.isEmpty()) {

                Identifier id = Identifier.parse(equipped);
                Item item = BuiltInRegistries.ITEM.getValue(id);

                if (item != null) {
                    data.equippedHat.put(uuid, item);
                }
            }
        }

        return data;
    }
}
