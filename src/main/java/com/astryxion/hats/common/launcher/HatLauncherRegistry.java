package com.astryxion.hats.common.launcher;

import com.astryxion.hats.Hats;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HatLauncherRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Hats.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Hats.MODID);

    public static final RegistryObject<Item> HAT_LAUNCHER = ITEMS.register(
            "hat_launcher",
            () -> new HatLauncherItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<EntityType<HatEntity>> HAT_ENTITY = ENTITIES.register(
            "hat",
            () -> EntityType.Builder.<HatEntity>of(HatEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(Hats.MODID + ":hat")
    );

    private HatLauncherRegistry() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }
}
