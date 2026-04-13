package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Handles progression milestones for Hunting Mode.
 * Milestones: 1 (New Wardrobe), 10, 100, 200, 300, 332 (Master Hatter).
 * Progression is disabled in Creative Mode to prevent accidental mass-unlocks.
 */
public class HatUnlockHandler {

    public static void onMobKilled(LivingEntity mob, DamageSource damageSource) {
        if (!(damageSource.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.isCreative()) {
            return;
        }

        ItemStack hatStack = HatManager.getHatStack(mob);

        if (hatStack.isEmpty()) {
            return;
        }

        ResourceLocation hatId = BuiltInRegistries.ITEM.getKey(hatStack.getItem());
        if (hatId == null || !hatId.getNamespace().equals(Hats.MODID)) {
            return;
        }

        final ItemStack finalStack = hatStack.copy();

        HatDataCapability.get(player).ifPresent(data -> {
            if (!data.hasHat(hatId)) {

                data.unlockHat(hatId);
                int totalUnlocked = data.getUnlockedHats().size();

                String advancementPath = null;

                if (totalUnlocked == 1) {
                    advancementPath = "hats/collect_1";
                } else if (totalUnlocked == 10) {
                    advancementPath = "hats/collect_10";
                } else if (totalUnlocked == 100) {
                    advancementPath = "hats/collect_100";
                } else if (totalUnlocked == 200) {
                    advancementPath = "hats/collect_200";
                } else if (totalUnlocked == 300) {
                    advancementPath = "hats/collect_300";
                } else if (totalUnlocked == 332) {
                    advancementPath = "hats/collect_332";
                }

                if (advancementPath != null) {
                    AdvancementHolder holder = player.getServer().getAdvancements()
                            .get(ResourceLocation.fromNamespaceAndPath(Hats.MODID, advancementPath));
                    if (holder != null) {
                        player.getAdvancements().award(holder, "unlock_via_code");
                    }
                }

                HatPacketHandler.sendHatUnlockedToPlayer(player, finalStack);
                HatPacketHandler.sendSyncHatToPlayer(player, data.serializeNBT());

                com.astryxion.hats.common.capability.HatDataCapability.markDirty(player);

                player.getInventory().setChanged();

                Hats.LOGGER.info("Unlocked hat: {} | Total: {}", hatId, totalUnlocked);
            }
        });

        HatManager.clearHat(mob);
    }
}
