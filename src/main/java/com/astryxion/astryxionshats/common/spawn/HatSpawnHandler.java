package com.astryxion.astryxionshats.common.spawn;

import com.astryxion.astryxionshats.Config;
import com.astryxion.astryxionshats.common.hat.HatManager;
import com.astryxion.astryxionshats.common.hat.HatPart;
import com.astryxion.astryxionshats.common.registry.HatItemRegistry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HatSpawnHandler {

    private static final Random RANDOM = new Random();

    /** Tracks which entities have already had a hat roll (per dimension+UUID) */
    private static final Set<String> HAT_ROLLED = ConcurrentHashMap.newKeySet();

    /** Tracks which entities have had spawn logic run (used when entity-join mixin is not available) */
    private static final Set<String> JOIN_PROCESSED = ConcurrentHashMap.newKeySet();

    /**
     * Call once per entity (e.g. first tick). Runs onEntityJoinWorld only the first time.
     */
    public static void onFirstTick(LivingEntity living) {
        String key = living.level().dimension().location() + ":" + living.getUUID();
        if (!JOIN_PROCESSED.add(key)) return;
        onEntityJoinWorld(living);
    }

    /**
     * Called when an entity is in the world (from first-tick or entity-join).
     */
    public static void onEntityJoinWorld(LivingEntity living) {

        if (!Config.enableHats || !Config.enableMobHats)
            return;

        if (living.getType() == EntityType.COW) tryEquipRandomHat(living);
        if (living.getType() == EntityType.SHEEP) tryEquipRandomHat(living);
        if (living.getType() == EntityType.PIG) tryEquipRandomHat(living);
        if (living.getType() == EntityType.CHICKEN) tryEquipRandomHat(living);

        if (living.getType() == EntityType.ZOMBIE) tryEquipRandomHat(living);
        if (living.getType() == EntityType.ZOMBIE_VILLAGER) tryEquipRandomHat(living);
        if (living.getType() == EntityType.SKELETON) tryEquipRandomHat(living);
        if (living.getType() == EntityType.STRAY) tryEquipRandomHat(living);
        if (living.getType() == EntityType.WITHER_SKELETON) tryEquipRandomHat(living);

        if (living.getType() == EntityType.HUSK) tryEquipRandomHat(living);
        if (living.getType() == EntityType.DROWNED) tryEquipRandomHat(living);

        if (living.getType() == EntityType.CREEPER) tryEquipRandomHat(living);
        if (living.getType() == EntityType.ENDERMAN) tryEquipRandomHat(living);

        if (living.getType() == EntityType.PILLAGER) tryEquipRandomHat(living);
        if (living.getType() == EntityType.VINDICATOR) tryEquipRandomHat(living);
        if (living.getType() == EntityType.EVOKER) tryEquipRandomHat(living);

        if (living.getType() == EntityType.VILLAGER) tryEquipRandomHat(living);
        if (living.getType() == EntityType.WANDERING_TRADER) tryEquipRandomHat(living);
        if (living.getType() == EntityType.IRON_GOLEM) tryEquipRandomHat(living);

        if (living.getType() == EntityType.PIGLIN) tryEquipRandomHat(living);
        if (living.getType() == EntityType.PIGLIN_BRUTE) tryEquipRandomHat(living);
        if (living.getType() == EntityType.ZOMBIFIED_PIGLIN) tryEquipRandomHat(living);

        if (living.getType() == EntityType.SQUID) tryEquipRandomHat(living);
        if (living.getType() == EntityType.GLOW_SQUID) tryEquipRandomHat(living);
    }

    private static void tryEquipRandomHat(LivingEntity entity) {
        String key = entity.level().dimension().location() + ":" + entity.getUUID();

        if (!HAT_ROLLED.add(key))
            return;

        if (HatManager.hasHat(entity))
            return;

        List<Item> hats = HatItemRegistry.getAllHats();

        if (hats.isEmpty())
            return;

        if (RANDOM.nextFloat() > Config.mobHatSpawnChance)
            return;

        Item randomHat = hats.get(RANDOM.nextInt(hats.size()));
        ItemStack hatStack = new ItemStack(randomHat);

        HatPart part = HatManager.get(entity);
        if (part != null) {
            part.setHatStack(hatStack.copy());
        }

        entity.setItemSlot(EquipmentSlot.HEAD, hatStack);
    }
}
