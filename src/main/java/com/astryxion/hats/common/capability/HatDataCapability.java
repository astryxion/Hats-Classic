package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.PlayerHatData;
import com.astryxion.hats.common.hat.PlayerHatDataImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Player hat data storage (NeoForge – SavedData + client cache)
 */
public class HatDataCapability {

    private static final String SAVED_DATA_ID = Hats.MODID + "_hat_data_v2";

    /** Client-side: local player's data (synced from server) */
    private static final PlayerHatDataImpl CLIENT_DATA = new PlayerHatDataImpl();

    public static Optional<PlayerHatData> get(Player player) {
        if (player == null) return Optional.empty();
        if (player.level().isClientSide()) {
            return Optional.of(CLIENT_DATA);
        }
        MinecraftServer server = player.getServer();
        if (server == null) return Optional.empty();
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) return Optional.empty();
        PlayerHatDataSavedData saved = getSavedData(overworld);
        return Optional.of(saved.getOrCreate(player.getUUID()));
    }

    public static void setClientData(CompoundTag nbt) {
        CLIENT_DATA.deserializeNBT(nbt);
    }

    /**
     * Mark saved data dirty so it is written to disk. Call after any server-side
     * change to player hat data (unlock, setEquipped, setSeenWelcome, copyFrom).
     */
    public static void markDirty(Player player) {
        if (player == null || player.level().isClientSide()) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) return;
        getSavedData(overworld).setDirty();
    }

    private static final SavedData.Factory<PlayerHatDataSavedData> FACTORY = new SavedData.Factory<>(
            PlayerHatDataSavedData::create,
            (tag, provider) -> tag.isEmpty() ? PlayerHatDataSavedData.create() : PlayerHatDataSavedData.load(tag),
            null
    );

    static PlayerHatDataSavedData getSavedData(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(FACTORY, SAVED_DATA_ID);
    }

    public static final class PlayerHatDataSavedData extends SavedData {
        private final Map<UUID, PlayerHatDataImpl> playerData = new HashMap<>();

        public static PlayerHatDataSavedData load(CompoundTag nbt) {
            PlayerHatDataSavedData data = new PlayerHatDataSavedData();
            CompoundTag players = nbt.getCompound("Players");
            for (String key : players.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    PlayerHatDataImpl impl = new PlayerHatDataImpl();
                    impl.deserializeNBT(players.getCompound(key));
                    data.playerData.put(uuid, impl);
                } catch (Exception ignored) {}
            }
            return data;
        }

        public static PlayerHatDataSavedData create() {
            return new PlayerHatDataSavedData();
        }

        @Override
        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, PlayerHatDataImpl> e : playerData.entrySet()) {
                players.put(e.getKey().toString(), e.getValue().serializeNBT());
            }
            nbt.put("Players", players);
            return nbt;
        }

        public PlayerHatDataImpl getOrCreate(UUID uuid) {
            return playerData.computeIfAbsent(uuid, k -> {
                setDirty();
                return new PlayerHatDataImpl();
            });
        }
    }
}
