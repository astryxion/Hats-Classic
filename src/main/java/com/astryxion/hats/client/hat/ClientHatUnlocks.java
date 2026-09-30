package com.astryxion.hats.client.hat;

import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.PlayerHatDataImpl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Client copy of the unlock list and stack counts.
 */
public final class ClientHatUnlocks {

    @Nullable
    private static LinkedHashMap<ResourceLocation, Integer> counts;
    private static final Set<ResourceLocation> recentlyGained = new LinkedHashSet<>();
    private static int generation;

    private ClientHatUnlocks() {}

    public static int generation() {
        return generation;
    }

    public static void ensureSeeded(@Nullable Player player) {
        if (counts != null || player == null) {
            return;
        }
        counts = new LinkedHashMap<>();
        player.getCapability(HatDataCapability.HAT_DATA).ifPresent(data -> {
            for (ResourceLocation id : data.getUnlockedHats()) {
                counts.put(id, 1);
            }
        });
    }

    public static void applySwap(Collection<ResourceLocation> gained, Collection<ResourceLocation> lost) {
        if (counts == null) {
            counts = new LinkedHashMap<>();
        }
        if (lost != null) {
            for (ResourceLocation id : lost) {
                increment(id, -1);
            }
        }
        if (gained != null) {
            for (ResourceLocation id : gained) {
                increment(id, 1);
                recentlyGained.add(id);
            }
        }
        generation++;
    }

    public static void acceptTrade(
            Collection<ResourceLocation> gained,
            Collection<ResourceLocation> lost,
            Collection<ResourceLocation> serverUnlocked
    ) {
        applySwap(gained, lost);
        if (serverUnlocked != null && !serverUnlocked.isEmpty() && containsAll(serverUnlocked, gained)) {
            forceSet(serverUnlocked);
        }
    }

    public static void forceSet(Collection<ResourceLocation> ids) {
        forceSet(ids, null);
    }

    public static void forceSet(Collection<ResourceLocation> ids, @Nullable List<Integer> amounts) {
        LinkedHashMap<ResourceLocation, Integer> next = new LinkedHashMap<>();
        if (ids != null) {
            int index = 0;
            for (ResourceLocation id : ids) {
                int amount = 1;
                if (amounts != null && index < amounts.size()) {
                    amount = Math.max(0, amounts.get(index));
                }
                if (id != null && amount > 0) {
                    next.put(id, amount);
                }
                index++;
            }
        }
        counts = next;
        recentlyGained.clear();
        generation++;
    }

    public static void forceSetIfComplete(Collection<ResourceLocation> ids, Collection<ResourceLocation> required) {
        forceSetIfComplete(ids, required, null);
    }

    public static void forceSetIfComplete(
            Collection<ResourceLocation> ids,
            Collection<ResourceLocation> required,
            @Nullable List<Integer> amounts
    ) {
        if (ids == null) {
            return;
        }
        if (required != null && !required.isEmpty() && !ids.containsAll(required)) {
            return;
        }
        forceSet(ids, amounts);
    }

    public static void set(Collection<ResourceLocation> ids) {
        if (ids == null || missingRecent(ids)) {
            return;
        }
        forceSet(ids);
    }

    public static void fromNbt(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        PlayerHatDataImpl data = new PlayerHatDataImpl();
        data.deserializeNBT(nbt);
        set(data.getUnlockedHats());
    }

    public static void clear() {
        counts = null;
        recentlyGained.clear();
        generation++;
    }

    public static boolean hasOverride() {
        return counts != null;
    }

    public static boolean has(ResourceLocation id) {
        return count(id) > 0;
    }

    public static int count(ResourceLocation id) {
        return counts == null || id == null ? 0 : counts.getOrDefault(id, 0);
    }

    public static Set<ResourceLocation> all() {
        return counts == null ? Set.of() : Collections.unmodifiableSet(counts.keySet());
    }

    public static Map<ResourceLocation, Integer> allCounts() {
        return counts == null ? Map.of() : Collections.unmodifiableMap(counts);
    }

    private static void increment(ResourceLocation id, int delta) {
        if (id == null || counts == null) {
            return;
        }
        int next = counts.getOrDefault(id, 0) + delta;
        if (next <= 0) {
            counts.remove(id);
        } else {
            counts.put(id, next);
        }
    }

    private static boolean missingRecent(Collection<ResourceLocation> ids) {
        return !recentlyGained.isEmpty() && !containsAll(ids, recentlyGained);
    }

    private static boolean containsAll(Collection<ResourceLocation> ids, Collection<ResourceLocation> required) {
        if (required == null || required.isEmpty()) {
            return true;
        }
        return ids != null && ids.containsAll(required);
    }
}
