package com.astryxion.hats.common.capability;

import com.astryxion.hats.common.hat.PlayerHatDataImpl;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

/**
 * Persists {@link PlayerHatDataImpl} with the same compound the packets and GUI already read.
 */
public class HatDataProvider implements IAttachmentSerializer<PlayerHatDataImpl> {

    @Override
    public PlayerHatDataImpl read(IAttachmentHolder holder, ValueInput input) {
        PlayerHatDataImpl data = new PlayerHatDataImpl();
        input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).ifPresent(data::deserializeNBT);
        return data;
    }

    @Override
    public boolean write(PlayerHatDataImpl data, ValueOutput output) {
        output.store(data.serializeNBT());
        return true;
    }
}
