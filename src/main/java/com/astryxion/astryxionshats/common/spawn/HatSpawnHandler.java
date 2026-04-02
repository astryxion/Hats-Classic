package com.astryxion.astryxionshats.common.spawn;

import com.astryxion.astryxionshats.AstryxionsHats;
import com.astryxion.astryxionshats.Config;
import com.astryxion.astryxionshats.common.hat.HatManager;
import com.astryxion.astryxionshats.common.hat.HatPart;
import com.astryxion.astryxionshats.common.registry.HatItemRegistry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = AstryxionsHats.MODID)
public class HatSpawnHandler {

    public static void onFirstTick(LivingEntity entity) {
    }

    private static final Random RANDOM = new Random();

    /** Persistent lock so mobs are evaluated ONCE only */
    private static final String HAT_ROLLED_KEY = "AstryxionHatRolled";

    /**
     * SMART TICK WIPE:
     * 1. Forces Zombies/Skeletons to burn by clearing the courier hat from the head slot.
     * 2. Frees up the head slot for Piglins to loot player armor.
     * 3. PROTECTS looted armor: If the item is NOT a mod hat, it won't be wiped.
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        // Server-side only
        if (entity.level().isClientSide())
            return;

        if (HatManager.isCustomOnly(entity)) {
            ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);

            if (!helmet.isEmpty() && HatManager.isModHat(helmet)) {
                entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
        }
    }

    // ==========================
    // MAIN SPAWN / LOAD EVENT
    // ==========================

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {

        if (event.getLevel().isClientSide())
            return;

        if (!Config.enableHats || !Config.enableMobHats)
            return;

        Entity entity = event.getEntity();

        if (!(entity instanceof LivingEntity living))
            return;

        // Enabled mobs
        if (entity.getType() == EntityType.COW) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.SHEEP) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.PIG) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.CHICKEN) tryEquipRandomHat(living);

        if (entity.getType() == EntityType.ZOMBIE) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.ZOMBIE_VILLAGER) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.SKELETON) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.STRAY) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.WITHER_SKELETON) tryEquipRandomHat(living);

        if (entity.getType() == EntityType.HUSK) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.DROWNED) tryEquipRandomHat(living);

        if (entity.getType() == EntityType.CREEPER) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.ENDERMAN) tryEquipRandomHat(living);

        if (entity.getType() == EntityType.PILLAGER) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.VINDICATOR) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.EVOKER) tryEquipRandomHat(living);

        if (entity.getType() == EntityType.VILLAGER) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.WANDERING_TRADER) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.IRON_GOLEM) tryEquipRandomHat(living);

        if (entity.getType() == EntityType.PIGLIN) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.PIGLIN_BRUTE) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.ZOMBIFIED_PIGLIN) tryEquipRandomHat(living);

        if (entity.getType() == EntityType.SQUID) tryEquipRandomHat(living);
        if (entity.getType() == EntityType.GLOW_SQUID) tryEquipRandomHat(living);
    }

    // ==========================
    // RANDOM HAT LOGIC (ONE-TIME)
    // ==========================

    private static void tryEquipRandomHat(LivingEntity entity) {

        var data = entity.getPersistentData();

        // HARD LOCK: if evaluated once, never reroll
        if (data.getBoolean(HAT_ROLLED_KEY))
            return;

        // Mark evaluated immediately (win or lose)
        data.putBoolean(HAT_ROLLED_KEY, true);

        // Respect existing hats
        if (HatManager.hasHat(entity))
            return;

        List<Item> hats = HatItemRegistry.ALL_HATS
                .stream()
                .map(reg -> reg.get())
                .toList();

        if (hats.isEmpty())
            return;

        if (RANDOM.nextFloat() > Config.mobHatSpawnChance)
            return;

        Item randomHat = hats.get(RANDOM.nextInt(hats.size()));
        ItemStack hatStack = new ItemStack(randomHat);

        // Store in capability
        HatPart part = HatManager.get(entity);
        if (part != null) {
            part.setHatStack(hatStack.copy());
        }

        // Courier helmet slot for client sync
        entity.setItemSlot(EquipmentSlot.HEAD, hatStack);
    }
}
