package com.astryxion.hats.common.data;

import com.astryxion.hats.common.capability.HatDataAccess;
import com.astryxion.hats.common.hat.PlayerHatData;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

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

    public static final Codec<ServerHatUnlocks> CODEC = CompoundTag.CODEC.xmap(
            ServerHatUnlocks::load,
            data -> data.save(new CompoundTag())
    );

    public static final SavedDataType<ServerHatUnlocks> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("hats", DATA_NAME),
            ServerHatUnlocks::new,
            CODEC
    );

    private final Map<UUID, LinkedHashMap<Identifier, Integer>> counts = new ConcurrentHashMap<>();

    public static ServerHatUnlocks get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static ServerHatUnlocks get(ServerPlayer player) {
        return get(player.level().getServer());
    }

    public Set<Identifier> copy(UUID uuid) {
        return new LinkedHashSet<>(owned(uuid).keySet());
    }

    public Set<Identifier> copy(ServerPlayer player) {
        ensureSeeded(player);
        return copy(player.getUUID());
    }

    public LinkedHashMap<Identifier, Integer> copyCounts(ServerPlayer player) {
        ensureSeeded(player);
        return new LinkedHashMap<>(owned(player.getUUID()));
    }

    public List<Integer> countsOf(Collection<Identifier> ids, ServerPlayer player) {
        ensureSeeded(player);
        LinkedHashMap<Identifier, Integer> owned = owned(player.getUUID());
        List<Integer> values = new ArrayList<>(ids.size());
        for (Identifier id : ids) {
            values.add(owned.getOrDefault(id, 0));
        }
        return values;
    }

    public int count(ServerPlayer player, Identifier id) {
        ensureSeeded(player);
        return id == null ? 0 : owned(player.getUUID()).getOrDefault(id, 0);
    }

    public void ensureSeeded(ServerPlayer player) {
        counts.computeIfAbsent(player.getUUID(), id -> {
            LinkedHashMap<Identifier, Integer> seeded = new LinkedHashMap<>();
            for (Identifier hat : HatDataAccess.snapshot(player)) {
                if (hat != null) {
                    seeded.put(hat, 1);
                }
            }
            return seeded;
        });
        setDirty();
    }

    public void put(ServerPlayer player, Collection<Identifier> ids, @Nullable Identifier equipped) {
        LinkedHashMap<Identifier, Integer> next = new LinkedHashMap<>();
        LinkedHashMap<Identifier, Integer> previous = owned(player.getUUID());
        for (Identifier id : ids) {
            if (id != null) {
                next.put(id, Math.max(1, previous.getOrDefault(id, 1)));
            }
        }
        counts.put(player.getUUID(), next);
        setDirty();
        writeCapability(player, next.keySet(), equipped);
    }

    public boolean add(ServerPlayer player, Identifier id) {
        return add(player, id, 1) == 1;
    }

    public int add(ServerPlayer player, Identifier id, int amount) {
        if (id == null || amount == 0) {
            return count(player, id);
        }
        ensureSeeded(player);
        LinkedHashMap<Identifier, Integer> owned = owned(player.getUUID());
        int next = owned.getOrDefault(id, 0) + amount;
        PlayerHatData data = HatDataAccess.get(player);
        Identifier equipped = data != null ? data.getEquippedHat() : null;
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

    public boolean removeHat(ServerPlayer player, Identifier id) {
        if (id == null) {
            return false;
        }
        ensureSeeded(player);
        LinkedHashMap<Identifier, Integer> owned = owned(player.getUUID());
        if (!owned.containsKey(id)) {
            return false;
        }
        owned.remove(id);
        PlayerHatData data = HatDataAccess.get(player);
        Identifier equipped = data != null ? data.getEquippedHat() : null;
        if (id.equals(equipped)) {
            equipped = null;
        }
        setDirty();
        writeCapability(player, owned.keySet(), equipped);
        return true;
    }

    public int clear(ServerPlayer player) {
        ensureSeeded(player);
        LinkedHashMap<Identifier, Integer> owned = owned(player.getUUID());
        int size = owned.size();
        owned.clear();
        setDirty();
        writeCapability(player, owned.keySet(), null);
        return size;
    }

    public boolean take(ServerPlayer player, Identifier id) {
        if (id == null) {
            return false;
        }
        ensureSeeded(player);
        LinkedHashMap<Identifier, Integer> owned = owned(player.getUUID());
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
        Identifier equipped = data != null ? data.getEquippedHat() : null;
        if (id.equals(equipped) && !owned.containsKey(id)) {
            equipped = null;
        }
        writeCapability(player, owned.keySet(), equipped);
        return true;
    }

    public boolean has(ServerPlayer player, Identifier id) {
        return count(player, id) > 0;
    }

    private LinkedHashMap<Identifier, Integer> owned(UUID uuid) {
        return counts.computeIfAbsent(uuid, key -> new LinkedHashMap<>());
    }

    private static void writeCapability(
            ServerPlayer player,
            Collection<Identifier> ids,
            @Nullable Identifier equipped
    ) {
        PlayerHatData data = HatDataAccess.get(player);
        if (data == null) {
            return;
        }
        for (Identifier id : Set.copyOf(data.getUnlockedHats())) {
            data.revokeHat(id);
        }
        for (Identifier id : ids) {
            data.grantHat(id);
        }
        data.setEquippedHat(equipped);
        HatDataAccess.replaceUnlocked(player, ids, equipped);
    }

    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        for (Map.Entry<UUID, LinkedHashMap<Identifier, Integer>> entry : counts.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putIntArray("Player", UUIDUtil.uuidToIntArray(entry.getKey()));
            ListTag hats = new ListTag();
            ListTag counted = new ListTag();
            for (Map.Entry<Identifier, Integer> hat : entry.getValue().entrySet()) {
                if (hat.getValue() == null || hat.getValue() <= 0) {
                    continue;
                }
                hats.add(hats.size(), StringTag.valueOf(hat.getKey().toString()));
                CompoundTag hatTag = new CompoundTag();
                hatTag.putString("Id", hat.getKey().toString());
                hatTag.putInt("Count", hat.getValue());
                counted.add(counted.size(), hatTag);
            }
            playerTag.put("Unlocked", hats);
            playerTag.put("Counts", counted);
            players.add(players.size(), playerTag);
        }
        tag.put("Players", players);
        return tag;
    }

    public static ServerHatUnlocks load(CompoundTag tag) {
        ServerHatUnlocks data = new ServerHatUnlocks();
        ListTag players = tag.getListOrEmpty("Players");
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompoundOrEmpty(i);
            int[] rawUuid = playerTag.getIntArray("Player").orElse(null);
            if (rawUuid == null || rawUuid.length != 4) {
                continue;
            }
            UUID uuid = UUIDUtil.uuidFromIntArray(rawUuid);
            LinkedHashMap<Identifier, Integer> hats = new LinkedHashMap<>();
            if (playerTag.contains("Counts")) {
                ListTag list = playerTag.getListOrEmpty("Counts");
                for (int j = 0; j < list.size(); j++) {
                    CompoundTag hatTag = list.getCompoundOrEmpty(j);
                    String idRaw = hatTag.getStringOr("Id", "");
                    if (idRaw.isEmpty()) {
                        continue;
                    }
                    Identifier id = Identifier.parse(idRaw);
                    hats.put(id, Math.max(1, hatTag.getIntOr("Count", 1)));
                }
            } else {
                ListTag list = playerTag.getListOrEmpty("Unlocked");
                for (int j = 0; j < list.size(); j++) {
                    String idRaw = list.getStringOr(j, "");
                    if (!idRaw.isEmpty()) {
                        hats.put(Identifier.parse(idRaw), 1);
                    }
                }
            }
            data.counts.put(uuid, hats);
        }
        return data;
    }
}
