package com.astryxion.astryxionshats.common.capability;

import com.astryxion.astryxionshats.common.hat.PlayerHatData;
import com.astryxion.astryxionshats.common.hat.PlayerHatDataImpl;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HatDataProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    private final PlayerHatDataImpl data = new PlayerHatDataImpl();
    private final LazyOptional<PlayerHatData> optional = LazyOptional.of(() -> data);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == HatDataCapability.HAT_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        // Delegate to the implementation we fixed earlier
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        // Delegate to the implementation we fixed earlier
        data.deserializeNBT(tag);
    }
}