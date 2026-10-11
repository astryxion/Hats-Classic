package com.astryxion.hats.common.spawn;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;
import com.astryxion.hats.Config;
import com.astryxion.hats.common.hat.HatAccessories;
import com.astryxion.hats.common.hat.HatContributor;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.registry.HatItemRegistry;
import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = Hats.MODID)
public class HatSpawnHandler {

    public static void onFirstTick(LivingEntity entity) {
    }

    private static final Random RANDOM = new Random();

    /** Persistent lock so mobs are evaluated ONCE only */
    private static final String HAT_ROLLED_KEY = "HatsModHatRolled";

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        if (entity.level().isClientSide()) {
            return;
        }
        HatManager.migrateAndStripCourierHat(entity);
        MobHatPolicy.apply(entity);
        clearUndeadSunlightFire(entity);
    }

    // ==========================
    // MAIN SPAWN / LOAD EVENT
    // ==========================

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {

        if (event.getLevel().isClientSide())
            return;

        Entity entity = event.getEntity();

        if (!(entity instanceof LivingEntity living))
            return;

        MobHatPolicy.apply(living);

        if (!Config.canMobSpawnWithHats(living.getType()))
            return;

        tryEquipRandomHat(living);
    }

    // ==========================
    // RANDOM HAT LOGIC (ONE-TIME)
    // ==========================

    private static void tryEquipRandomHat(LivingEntity entity) {

        var data = entity.getPersistentData();

        // HARD LOCK: if evaluated once, never reroll
        if (data.getBooleanOr(HAT_ROLLED_KEY, false))
            return;

        // Mark evaluated immediately (win or lose)
        data.putBoolean(HAT_ROLLED_KEY, true);

        // Respect existing hats
        if (HatManager.hasHat(entity))
            return;

        List<Item> hats = spawnCandidateHats();
        if (hats.isEmpty())
            return;

        if (RANDOM.nextFloat() > Config.mobHatSpawnChance)
            return;

        Item randomHat = hats.get(RANDOM.nextInt(hats.size()));
        ItemStack stack = new ItemStack(randomHat);
        TabulaHatDefinition tabula = TabulaHats.from(stack);
        if (tabula != null && !tabula.accessories.isEmpty()) {
            HatAccessories.applyRandom(stack, tabula, RANDOM);
        }
        HatManager.setHatStack(entity, stack);
    }

    private static List<Item> spawnCandidateHats() {
        List<Item> hats = new ArrayList<>();
        for (var reg : HatItemRegistry.ALL_HATS) {
            Item item = reg.get();
            if (Config.disableContributorHats && HatContributor.isContributor(item)) {
                continue;
            }
            hats.add(item);
        }
        return hats;
    }

    /**
     * Renewed parity: hatted undead that would burn in sunlight have fire cleared.
     */
    private static void clearUndeadSunlightFire(LivingEntity entity) {
        if (!Config.preventUndeadFire) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level) || !level.isBrightOutside()) {
            return;
        }
        if (!entity.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
            return;
        }
        if (!HatManager.hasHat(entity) || !entity.isOnFire()) {
            return;
        }
        if (entity.isInWaterOrRain() || entity.isInPowderSnow) {
            return;
        }
        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        if (!level.canSeeSky(eyePos)) {
            return;
        }
        entity.clearFire();
    }
}
