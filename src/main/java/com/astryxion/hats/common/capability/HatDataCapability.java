package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.PlayerHatData;
import com.astryxion.hats.common.hat.PlayerHatDataImpl;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Player hat data storage (NeoForge – SavedData + client cache)
 */
public class HatDataCapability {

    public static final SavedDataType<PlayerHatDataSavedData> SAVED_DATA_TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Hats.MODID, "hat_data_v2"),
            PlayerHatDataSavedData::create,
            PlayerHatDataSavedData.CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    /** Client-side: local player's data (synced from server) */
    private static final PlayerHatDataImpl CLIENT_DATA = new PlayerHatDataImpl();

    public static Optional<PlayerHatData> get(Player player) {
        if (player == null) return Optional.empty();
        if (player.level().isClientSide()) {
            return Optional.of(CLIENT_DATA);
        }
        MinecraftServer server = player.level().getServer();
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
        MinecraftServer server = player.level().getServer();
        if (server == null) return;
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) return;
        getSavedData(overworld).setDirty(true);
    }

    static PlayerHatDataSavedData getSavedData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(SAVED_DATA_TYPE);
    }

    public static final class PlayerHatDataSavedData extends SavedData {
        public static final Codec<PlayerHatDataSavedData> CODEC = ExtraCodecs.converter(NbtOps.INSTANCE)
                .xmap(PlayerHatDataSavedData::decode, PlayerHatDataSavedData::encodeRoot);

        private final Map<UUID, PlayerHatDataImpl> playerData = new HashMap<>();

        public static PlayerHatDataSavedData decode(Tag tag) {
            return load((CompoundTag) tag);
        }

        public CompoundTag encodeRoot() {
            CompoundTag nbt = new CompoundTag();
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, PlayerHatDataImpl> e : playerData.entrySet()) {
                players.put(e.getKey().toString(), e.getValue().serializeNBT());
            }
            nbt.put("Players", players);
            return nbt;
        }

        public static PlayerHatDataSavedData load(CompoundTag nbt) {
            PlayerHatDataSavedData data = new PlayerHatDataSavedData();
            CompoundTag players = nbt.getCompoundOrEmpty("Players");
            for (String key : players.keySet()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    PlayerHatDataImpl impl = new PlayerHatDataImpl();
                    impl.deserializeNBT(players.getCompoundOrEmpty(key));
                    data.playerData.put(uuid, impl);
                } catch (Exception ignored) {}
            }
            return data;
        }

        public static PlayerHatDataSavedData create() {
            return new PlayerHatDataSavedData();
        }

        public PlayerHatDataImpl getOrCreate(UUID uuid) {
            return playerData.computeIfAbsent(uuid, k -> {
                setDirty(true);
                return new PlayerHatDataImpl();
            });
        }
    }
}
