package com.astryxion.hats.common.data;

import com.astryxion.hats.Hats;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.ExtraCodecs;
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

    public static final SavedDataType<PlayerHatInventory> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Hats.MODID, "hats_inventory"),
            PlayerHatInventory::new,
            PlayerHatInventory.CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    public static final Codec<PlayerHatInventory> CODEC = ExtraCodecs.converter(NbtOps.INSTANCE)
            .xmap(PlayerHatInventory::decode, PlayerHatInventory::encodeRoot);

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

    public static PlayerHatInventory decode(Tag tag) {
        return load((CompoundTag) tag);
    }

    public CompoundTag encodeRoot() {
        CompoundTag tag = new CompoundTag();
        ListTag playersList = new ListTag();

        for (UUID uuid : unlockedHats.keySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putString("Player", uuid.toString());

            ListTag hatsTag = new ListTag();
            for (Item hat : unlockedHats.get(uuid)) {
                Identifier id = BuiltInRegistries.ITEM.getKey(hat);
                if (id != null) {
                    hatsTag.add(StringTag.valueOf(id.toString()));
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

            playersList.add(playerTag);
        }

        tag.put("Players", playersList);
        return tag;
    }

    // ============================
    // Unlock logic
    // ============================

    public void unlockHat(UUID player, Item hat) {

        if (hat == null || hat == Items.AIR) return;

        unlockedHats
                .computeIfAbsent(player, id -> new HashSet<>())
                .add(hat);

        setDirty(true);
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

        setDirty(true);
    }

    public Item getEquippedHat(UUID player) {

        return equippedHat.getOrDefault(player, Items.AIR);
    }

    public void clearEquippedHat(UUID player) {

        equippedHat.remove(player);
        setDirty(true);
    }

    // ============================
    // Loading
    // ============================

    public static PlayerHatInventory load(CompoundTag tag) {

        PlayerHatInventory data = new PlayerHatInventory();

        ListTag playersList = tag.getListOrEmpty("Players");

        for (int i = 0; i < playersList.size(); i++) {

            CompoundTag playerTag = playersList.getCompound(i).orElseThrow();

            UUID uuid = UUID.fromString(playerTag.getString("Player").orElseThrow());

            Set<Item> hats = new HashSet<>();
            ListTag hatsTag = playerTag.getListOrEmpty("Unlocked");

            for (int j = 0; j < hatsTag.size(); j++) {

                Identifier id = Identifier.parse(hatsTag.getString(j).orElseThrow());
                Item item = BuiltInRegistries.ITEM.getValue(ResourceKey.create(Registries.ITEM, id));

                if (item != null) {
                    hats.add(item);
                }
            }

            data.unlockedHats.put(uuid, hats);

            if (playerTag.getString("Equipped").isPresent()) {

                Identifier id = Identifier.parse(playerTag.getString("Equipped").orElseThrow());
                Item item = BuiltInRegistries.ITEM.getValue(ResourceKey.create(Registries.ITEM, id));

                if (item != null) {
                    data.equippedHat.put(uuid, item);
                }
            }
        }

        return data;
    }
}
