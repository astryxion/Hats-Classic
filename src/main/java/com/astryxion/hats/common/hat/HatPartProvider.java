package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;
import com.mojang.serialization.MapCodec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class HatPartProvider implements IAttachmentSerializer<HatPart> {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(Hats.MODID, "hat_part");

    @Override
    public HatPart read(IAttachmentHolder holder, ValueInput input) {
        HatPart hatPart = new HatPart();
        input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).ifPresent(hatPart::deserializeNBT);
        return hatPart;
    }

    @Override
    public boolean write(HatPart hatPart, ValueOutput output) {
        output.store(hatPart.serializeNBT());
        return true;
    }
}
