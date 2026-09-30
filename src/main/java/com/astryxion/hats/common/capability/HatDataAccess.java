package com.astryxion.hats.common.capability;

import com.astryxion.hats.common.hat.PlayerHatData;
import com.astryxion.hats.common.hat.PlayerHatDataImpl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Players currently have two {@code HAT_DATA} providers ({@code hat_data} and
 * {@code hat_data_v2}). {@code getCapability} only returns one of them, so a
 * trade can write the swap to an instance the GUI never reads. This tracks
 * every attached instance and writes the same NBT into both persisted blobs.
 */
public final class HatDataAccess {

    private static final String[] CAP_KEYS = {
            "hats:hat_data",
            "hats:hat_data_v2"
    };

    private static final Map<Player, List<PlayerHatData>> INSTANCES = new WeakHashMap<>();

    private static final Method SERIALIZE_CAPS = findCapsMethod("serializeCaps");
    private static final Method DESERIALIZE_CAPS = findCapsMethod("deserializeCaps", CompoundTag.class);

    private HatDataAccess() {}

    public static void register(Player player, PlayerHatData data) {
        if (player == null || data == null) {
            return;
        }
        INSTANCES.computeIfAbsent(player, ignored -> new ArrayList<>()).add(data);
    }

    public static List<PlayerHatData> all(Player player) {
        List<PlayerHatData> registered = player == null ? null : INSTANCES.get(player);
        if (registered != null && !registered.isEmpty()) {
            return List.copyOf(registered);
        }
        PlayerHatData data = get(player);
        return data == null ? List.of() : List.of(data);
    }

    public static void forEach(Player player, Consumer<PlayerHatData> action) {
        for (PlayerHatData data : all(player)) {
            action.accept(data);
        }
    }

    @Nullable
    public static PlayerHatData get(Player player) {
        return player == null ? null : player.getCapability(HatDataCapability.HAT_DATA).orElse(null);
    }

    public static boolean has(Player player, ResourceLocation id) {
        if (id == null) {
            return false;
        }
        for (PlayerHatData data : all(player)) {
            if (data.getUnlockedHats().contains(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * One player's unlocks only. Never union the two HatData copies — a stale
     * second copy still holding the old hat is why both traders ended up with
     * Dragon Skull.
     */
    public static Set<ResourceLocation> snapshot(Player player) {
        return snapshotOwning(player, List.of());
    }

    public static Set<ResourceLocation> snapshotOwning(Player player, Collection<ResourceLocation> required) {
        PlayerHatData match = null;
        if (required != null && !required.isEmpty()) {
            for (PlayerHatData data : all(player)) {
                if (data.getUnlockedHats().containsAll(required)) {
                    match = data;
                    break;
                }
            }
        }
        if (match == null) {
            match = get(player);
        }
        return match == null ? new LinkedHashSet<>() : new LinkedHashSet<>(match.getUnlockedHats());
    }

    public static CompoundTag nbtOf(Player player, Collection<ResourceLocation> unlocked, @Nullable ResourceLocation equipped) {
        PlayerHatData current = get(player);
        CompoundTag nbt = current != null ? current.serializeNBT() : new PlayerHatDataImpl().serializeNBT();
        ListTag list = new ListTag();
        for (ResourceLocation id : unlocked) {
            if (id != null) {
                list.add(StringTag.valueOf(id.toString()));
            }
        }
        nbt.put("UnlockedHats", list);
        if (equipped != null) {
            nbt.putString("EquippedHat", equipped.toString());
        } else {
            nbt.remove("EquippedHat");
        }
        return nbt;
    }

    public static CompoundTag primaryNbt(Player player) {
        PlayerHatData data = get(player);
        return data != null ? data.serializeNBT() : new PlayerHatDataImpl().serializeNBT();
    }

    public static void apply(Player player, CompoundTag hatNbt) {
        if (player == null || hatNbt == null) {
            return;
        }
        forEach(player, data -> data.deserializeNBT(hatNbt));
        player.getCapability(HatDataCapability.HAT_DATA).ifPresent(data -> data.deserializeNBT(hatNbt));
        if (player.level().isClientSide()) {
            return;
        }
        try {
            if (SERIALIZE_CAPS == null || DESERIALIZE_CAPS == null) {
                return;
            }
            CompoundTag caps = (CompoundTag) SERIALIZE_CAPS.invoke(player);
            if (caps != null && caps.contains("ForgeCaps")) {
                CompoundTag forge = caps.getCompound("ForgeCaps");
                for (String key : CAP_KEYS) {
                    forge.put(key, hatNbt.copy());
                }
                DESERIALIZE_CAPS.invoke(player, caps);
            }
        } catch (Throwable ignored) {
        }
        forEach(player, data -> data.deserializeNBT(hatNbt));
        player.getCapability(HatDataCapability.HAT_DATA).ifPresent(data -> data.deserializeNBT(hatNbt));
    }

    public static void replaceUnlocked(Player player, Collection<ResourceLocation> unlocked, @Nullable ResourceLocation equipped) {
        if (player == null) {
            return;
        }
        PlayerHatData current = get(player);
        CompoundTag nbt = current != null ? current.serializeNBT() : new PlayerHatDataImpl().serializeNBT();
        ListTag list = new ListTag();
        Set<ResourceLocation> unique = new LinkedHashSet<>(unlocked);
        for (ResourceLocation id : unique) {
            if (id != null) {
                list.add(StringTag.valueOf(id.toString()));
            }
        }
        nbt.put("UnlockedHats", list);
        if (equipped != null) {
            nbt.putString("EquippedHat", equipped.toString());
        } else {
            nbt.remove("EquippedHat");
        }
        apply(player, nbt);
    }

    public static List<ResourceLocation> copyUnlocked(Player player) {
        return List.copyOf(snapshot(player));
    }

    public static List<ResourceLocation> copyUnlocked(PlayerHatData data) {
        return data == null ? List.of() : List.copyOf(data.getUnlockedHats());
    }

    @Nullable
    private static Method findCapsMethod(String name, Class<?>... args) {
        Class<?> search = Player.class;
        while (search != null && search != Object.class) {
            try {
                Method method = search.getDeclaredMethod(name, args);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                search = search.getSuperclass();
            }
        }
        return null;
    }
}
