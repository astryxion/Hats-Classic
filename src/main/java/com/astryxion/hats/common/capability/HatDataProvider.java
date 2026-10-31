package com.astryxion.hats.common.capability;

import com.astryxion.hats.common.hat.PlayerHatDataImpl;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class HatDataProvider implements IAttachmentSerializer<CompoundTag, PlayerHatDataImpl> {

    @Override
    public PlayerHatDataImpl read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
        PlayerHatDataImpl data = new PlayerHatDataImpl();
        data.deserializeNBT(tag);
        return data;
    }

    @Override
    public CompoundTag write(PlayerHatDataImpl data, HolderLookup.Provider provider) {
        return data.serializeNBT();
    }
}
