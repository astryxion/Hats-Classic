package com.astryxion.hats.common.sound;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.Hats;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class HatsSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Hats.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TUBE = register("tube");
    public static final DeferredHolder<SoundEvent, SoundEvent> POOF = register("poof");
    public static final DeferredHolder<SoundEvent, SoundEvent> BONK = register("bonk");

    private HatsSounds() {}

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(Hats.MODID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
