package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HatPartProvider implements ICapabilitySerializable<CompoundTag> {

    public static final ResourceLocation ID =
            new ResourceLocation(Hats.MODID, "hat_part");

    private final HatPart hatPart = new HatPart();
    private final LazyOptional<HatPart> optional = LazyOptional.of(() -> hatPart);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> cap,
            @Nullable Direction side
    ) {
        return cap == HatPartCapability.HAT_PART
                ? optional.cast()
                : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return hatPart.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        hatPart.deserializeNBT(nbt);
    }
}
