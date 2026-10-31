package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Holds the HatPart attachment reference ONLY
 * (No syncing logic here – handled by ServerHatHandler)
 */
public class HatPartCapability {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Hats.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HatPart>> HAT_PART =
            ATTACHMENT_TYPES.register("hat_part", () -> AttachmentType.builder(HatPart::new)
                    .serialize(new HatPartProvider())
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
