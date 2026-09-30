package com.astryxion.hats.common.sound;

import com.astryxion.hats.Hats;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HatsSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Hats.MODID);

    public static final RegistryObject<SoundEvent> TUBE = register("tube");
    public static final RegistryObject<SoundEvent> POOF = register("poof");
    public static final RegistryObject<SoundEvent> BONK = register("bonk");

    private HatsSounds() {}

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(Hats.MODID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
