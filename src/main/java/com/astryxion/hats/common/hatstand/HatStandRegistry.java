package com.astryxion.hats.common.hatstand;

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

public final class HatStandRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Hats.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, Hats.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<HatStandEntity>> HAT_STAND_ENTITY = ENTITIES.register(
            "hat_stand",
            () -> EntityType.Builder.<HatStandEntity>of(HatStandEntity::new, MobCategory.MISC)
                    .sized(9.0F / 16.0F, 11.0F / 16.0F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Hats.MODID, "hat_stand")))
    );

    public static final DeferredHolder<Item, Item> HAT_STAND_ITEM = ITEMS.register(
            "hat_stand",
            id -> new HatStandItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(16))
    );

    private HatStandRegistry() {}

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        ITEMS.register(bus);
    }
}
