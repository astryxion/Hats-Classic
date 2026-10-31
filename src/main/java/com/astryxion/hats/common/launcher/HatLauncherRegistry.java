package com.astryxion.hats.common.launcher;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import com.astryxion.hats.Hats;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class HatLauncherRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, Hats.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Hats.MODID);

    public static final DeferredHolder<Item, Item> HAT_LAUNCHER = ITEMS.register(
            "hat_launcher",
            id -> new HatLauncherItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(1))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<HatEntity>> HAT_ENTITY = ENTITIES.register(
            "hat",
            () -> EntityType.Builder.<HatEntity>of(HatEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Hats.MODID, "hat")))
    );

    private HatLauncherRegistry() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }
}
