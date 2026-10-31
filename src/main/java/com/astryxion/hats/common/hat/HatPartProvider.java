package com.astryxion.hats.common.hat;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class HatPartProvider implements IAttachmentSerializer<CompoundTag, HatPart> {

    @Override
    public HatPart read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
        HatPart part = new HatPart();
        part.deserializeNBT(tag);
        return part;
    }

    @Override
    public CompoundTag write(HatPart part, HolderLookup.Provider provider) {
        return part.serializeNBT();
    }
}
