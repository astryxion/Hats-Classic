package com.astryxion.hats.common.data;

import com.astryxion.hats.common.capability.HatDataAccess;
import com.astryxion.hats.common.hat.PlayerHatData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-UUID unlocks with stackable counts. Trades and the launcher spend one
 * copy. Relog reads this first, then mirrors owned ids onto the capability.
 */
public final class ServerHatUnlocks extends SavedData {

    private static final String DATA_NAME = "hats_unlocks";

    private final Map<UUID, LinkedHashMap<ResourceLocation, Integer>> counts = new ConcurrentHashMap<>();

    public static ServerHatUnlocks get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                ServerHatUnlocks::load, ServerHatUnlocks::new, DATA_NAME);
    }

    public static ServerHatUnlocks get(ServerPlayer player) {
        return get(player.getServer());
    }

    public Set<ResourceLocation> copy(UUID uuid) {
        return new LinkedHashSet<>(owned(uuid).keySet());
    }

    public Set<ResourceLocation> copy(ServerPlayer player) {
        ensureSeeded(player);
        return copy(player.getUUID());
    }

    public LinkedHashMap<ResourceLocation, Integer> copyCounts(ServerPlayer player) {
        ensureSeeded(player);
        return new LinkedHashMap<>(owned(player.getUUID()));
    }

    public List<Integer> countsOf(Collection<ResourceLocation> ids, ServerPlayer player) {
        ensureSeeded(player);
        LinkedHashMap<ResourceLocation, Integer> owned = owned(player.getUUID());
        List<Integer> values = new ArrayList<>(ids.size());
        for (ResourceLocation id : ids) {
            values.add(owned.getOrDefault(id, 0));
        }
        return values;
    }

    public int count(ServerPlayer player, ResourceLocation id) {
        ensureSeeded(player);
        return id == null ? 0 : owned(player.getUUID()).getOrDefault(id, 0);
    }

    public void ensureSeeded(ServerPlayer player) {
        counts.computeIfAbsent(player.getUUID(), id -> {
            LinkedHashMap<ResourceLocation, Integer> seeded = new LinkedHashMap<>();
            for (ResourceLocation hat : HatDataAccess.snapshot(player)) {
                if (hat != null) {
                    seeded.put(hat, 1);
                }
            }
            return seeded;
        });
        setDirty();
    }

    public void put(ServerPlayer player, Collection<ResourceLocation> ids, @Nullable ResourceLocation equipped) {
        LinkedHashMap<ResourceLocation, Integer> next = new LinkedHashMap<>();
        LinkedHashMap<ResourceLocation, Integer> previous = owned(player.getUUID());
        for (ResourceLocation id : ids) {
            if (id != null) {
                next.put(id, Math.max(1, previous.getOrDefault(id, 1)));
            }
        }
        counts.put(player.getUUID(), next);
        setDirty();
        writeCapability(player, next.keySet(), equipped);
    }

    public boolean add(ServerPlayer player, ResourceLocation id) {
        return add(player, id, 1) == 1;
    }

    public int add(ServerPlayer player, ResourceLocation id, int amount) {
        if (id == null || amount == 0) {
            return count(player, id);
        }
        ensureSeeded(player);
        LinkedHashMap<ResourceLocation, Integer> owned = owned(player.getUUID());
        int next = owned.getOrDefault(id, 0) + amount;
        PlayerHatData data = HatDataAccess.get(player);
        ResourceLocation equipped = data != null ? data.getEquippedHat() : null;
        if (next <= 0) {
            owned.remove(id);
            if (id.equals(equipped)) {
                equipped = null;
            }
            next = 0;
        } else {
            owned.put(id, next);
        }
        setDirty();
        writeCapability(player, owned.keySet(), equipped);
        return next;
    }

    public boolean removeHat(ServerPlayer player, ResourceLocation id) {
        if (id == null) {
            return false;
        }
        ensureSeeded(player);
        LinkedHashMap<ResourceLocation, Integer> owned = owned(player.getUUID());
        if (!owned.containsKey(id)) {
            return false;
        }
        owned.remove(id);
        PlayerHatData data = HatDataAccess.get(player);
        ResourceLocation equipped = data != null ? data.getEquippedHat() : null;
        if (id.equals(equipped)) {
            equipped = null;
        }
        setDirty();
        writeCapability(player, owned.keySet(), equipped);
        return true;
    }

    public int clear(ServerPlayer player) {
        ensureSeeded(player);
        LinkedHashMap<ResourceLocation, Integer> owned = owned(player.getUUID());
        int size = owned.size();
        owned.clear();
        setDirty();
        writeCapability(player, owned.keySet(), null);
        return size;
    }

    public boolean take(ServerPlayer player, ResourceLocation id) {
        if (id == null) {
            return false;
        }
        ensureSeeded(player);
        LinkedHashMap<ResourceLocation, Integer> owned = owned(player.getUUID());
        int current = owned.getOrDefault(id, 0);
        if (current <= 0) {
            return false;
        }
        if (current == 1) {
            owned.remove(id);
        } else {
            owned.put(id, current - 1);
        }
        setDirty();
        PlayerHatData data = HatDataAccess.get(player);
        ResourceLocation equipped = data != null ? data.getEquippedHat() : null;
        if (id.equals(equipped) && !owned.containsKey(id)) {
            equipped = null;
        }
        writeCapability(player, owned.keySet(), equipped);
        return true;
    }

    public boolean has(ServerPlayer player, ResourceLocation id) {
        return count(player, id) > 0;
    }

    private LinkedHashMap<ResourceLocation, Integer> owned(UUID uuid) {
        return counts.computeIfAbsent(uuid, key -> new LinkedHashMap<>());
    }

    private static void writeCapability(
            ServerPlayer player,
            Collection<ResourceLocation> ids,
            @Nullable ResourceLocation equipped
    ) {
        PlayerHatData data = HatDataAccess.get(player);
        if (data == null) {
            return;
        }
        for (ResourceLocation id : Set.copyOf(data.getUnlockedHats())) {
            data.revokeHat(id);
        }
        for (ResourceLocation id : ids) {
            data.grantHat(id);
        }
        data.setEquippedHat(equipped);
        HatDataAccess.replaceUnlocked(player, ids, equipped);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        for (Map.Entry<UUID, LinkedHashMap<ResourceLocation, Integer>> entry : counts.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            ListTag hats = new ListTag();
            ListTag counted = new ListTag();
            for (Map.Entry<ResourceLocation, Integer> hat : entry.getValue().entrySet()) {
                if (hat.getValue() == null || hat.getValue() <= 0) {
                    continue;
                }
                hats.add(StringTag.valueOf(hat.getKey().toString()));
                CompoundTag hatTag = new CompoundTag();
                hatTag.putString("Id", hat.getKey().toString());
                hatTag.putInt("Count", hat.getValue());
                counted.add(hatTag);
            }
            playerTag.put("Unlocked", hats);
            playerTag.put("Counts", counted);
            players.add(playerTag);
        }
        tag.put("Players", players);
        return tag;
    }

    public static ServerHatUnlocks load(CompoundTag tag) {
        ServerHatUnlocks data = new ServerHatUnlocks();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            UUID uuid = playerTag.getUUID("Player");
            LinkedHashMap<ResourceLocation, Integer> hats = new LinkedHashMap<>();
            if (playerTag.contains("Counts", Tag.TAG_LIST)) {
                ListTag list = playerTag.getList("Counts", Tag.TAG_COMPOUND);
                for (int j = 0; j < list.size(); j++) {
                    CompoundTag hatTag = list.getCompound(j);
                    ResourceLocation id = new ResourceLocation(hatTag.getString("Id"));
                    hats.put(id, Math.max(1, hatTag.getInt("Count")));
                }
            } else {
                ListTag list = playerTag.getList("Unlocked", Tag.TAG_STRING);
                for (int j = 0; j < list.size(); j++) {
                    hats.put(new ResourceLocation(list.getString(j)), 1);
                }
            }
            data.counts.put(uuid, hats);
        }
        return data;
    }
}
