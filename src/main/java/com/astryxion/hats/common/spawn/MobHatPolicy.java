package com.astryxion.hats.common.spawn;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.hat.HatManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * When a mob type is config-disabled (per-mob toggle or Boss Mobs off), its
 * hat is cleared but kept in entity NBT so re-enabling restores the same stack.
 */
public final class MobHatPolicy {

    private static final String STASH_KEY = "HatsConfigHatStash";
    /** Pre-rename key; still read so existing boss stashes survive the update. */
    private static final String LEGACY_BOSS_STASH_KEY = "HatsBossHatStash";

    private MobHatPolicy() {
    }

    public static void apply(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        if (!Config.MOB_HAT_TOGGLES.containsKey(Config.mobKey(entity.getType()))) {
            return;
        }

        CompoundTag data = entity.getPersistentData();
        migrateLegacyStash(data);
        if (!Config.canMobSpawnWithHats(entity.getType())) {
            stashAndClear(entity, data);
        } else {
            restoreStash(entity, data);
        }
    }

    /** Apply to every loaded living entity on the integrated/dedicated server. */
    public static void applyToLoadedWorlds() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            for (ServerLevel level : server.getAllLevels()) {
                for (var entity : level.getAllEntities()) {
                    if (entity instanceof LivingEntity living) {
                        apply(living);
                    }
                }
            }
        });
    }

    private static void migrateLegacyStash(CompoundTag data) {
        if (data.contains(STASH_KEY) || !data.contains(LEGACY_BOSS_STASH_KEY)) {
            return;
        }
        data.put(STASH_KEY, data.getCompound(LEGACY_BOSS_STASH_KEY));
        data.remove(LEGACY_BOSS_STASH_KEY);
    }

    private static void stashAndClear(LivingEntity entity, CompoundTag data) {
        if (!HatManager.hasHat(entity)) {
            return;
        }
        ItemStack hat = HatManager.getHatStack(entity);
        if (hat.isEmpty()) {
            return;
        }
        data.put(STASH_KEY, hat.save(new CompoundTag()));
        HatManager.clearHat(entity);
    }

    private static void restoreStash(LivingEntity entity, CompoundTag data) {
        if (!data.contains(STASH_KEY)) {
            return;
        }
        ItemStack stashed = ItemStack.of(data.getCompound(STASH_KEY));
        data.remove(STASH_KEY);
        if (stashed.isEmpty() || HatManager.hasHat(entity)) {
            return;
        }
        HatManager.setHatStack(entity, stashed);
    }
}
