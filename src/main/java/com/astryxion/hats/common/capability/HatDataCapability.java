package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.PlayerHatDataImpl;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Attachment definition for player hat data.
 * Replaces the Forge capability token. The saved keys stay UnlockedHats, EquippedHat, and SeenWelcome.
 */
public class HatDataCapability {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Hats.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerHatDataImpl>> HAT_DATA =
            ATTACHMENT_TYPES.register("hat_data", () -> AttachmentType.builder(PlayerHatDataImpl::new)
                    .serialize(new HatDataProvider())
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
