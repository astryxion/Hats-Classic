package com.astryxion.astryxionshats.common.capability;

import com.astryxion.astryxionshats.common.hat.PlayerHatData;
import com.astryxion.astryxionshats.common.hat.PlayerHatDataImpl;
import net.minecraft.nbt.CompoundTag;

/**
 * Fabric: PlayerHatData is accessed via HatDataCapability.get(Player).
 * This class is kept for reference; serialization is in PlayerHatDataImpl.
 */
public final class HatDataProvider {

    public static PlayerHatData createNew() {
        return new PlayerHatDataImpl();
    }

    public static CompoundTag serialize(PlayerHatData data) {
        return data.serializeNBT();
    }

    public static void deserialize(PlayerHatData data, CompoundTag tag) {
        data.deserializeNBT(tag);
    }

    private HatDataProvider() {}
}
