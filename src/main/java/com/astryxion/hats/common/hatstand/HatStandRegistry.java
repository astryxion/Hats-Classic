package com.astryxion.hats.common.hatstand;

import com.astryxion.hats.Hats;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HatStandRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Hats.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Hats.MODID);

    public static final RegistryObject<EntityType<HatStandEntity>> HAT_STAND_ENTITY = ENTITIES.register(
            "hat_stand",
            () -> EntityType.Builder.<HatStandEntity>of(HatStandEntity::new, MobCategory.MISC)
                    .sized(9.0F / 16.0F, 11.0F / 16.0F)
                    .clientTrackingRange(10)
                    .build(Hats.MODID + ":hat_stand")
    );

    public static final RegistryObject<Item> HAT_STAND_ITEM = ITEMS.register(
            "hat_stand",
            () -> new HatStandItem(new Item.Properties().stacksTo(16))
    );

    private HatStandRegistry() {}

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        ITEMS.register(bus);
    }
}
