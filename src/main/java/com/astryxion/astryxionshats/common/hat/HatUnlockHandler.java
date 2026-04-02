package com.astryxion.astryxionshats.common.hat;

import com.astryxion.astryxionshats.AstryxionsHats;
import com.astryxion.astryxionshats.common.capability.HatDataCapability;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;
import com.astryxion.astryxionshats.common.network.PacketHatUnlocked;
import com.astryxion.astryxionshats.common.network.PacketSyncHat;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Handles progression milestones for Hunting Mode.
 * Milestones: 1 (New Wardrobe), 10, 100, 200, 300, 332 (Master Hatter).
 * Progression is disabled in Creative Mode to prevent accidental mass-unlocks.
 */
@Mod.EventBusSubscriber(modid = AstryxionsHats.MODID)
public class HatUnlockHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 🛑 CREATIVE CHECK: Exit immediately if player is in Creative Mode.
        // This is the most reliable method in 1.20.1 to avoid mapping errors.
        if (player.isCreative()) {
            return;
        }

        LivingEntity mob = event.getEntity();

        // 1. Get the hat stack from the custom slot
        ItemStack hatStack = HatManager.getHatStack(mob);

        // 2. Validation
        if (hatStack.isEmpty()) {
            return;
        }

        ResourceLocation hatId = ForgeRegistries.ITEMS.getKey(hatStack.getItem());
        if (hatId == null || !hatId.getNamespace().equals(AstryxionsHats.MODID)) {
            return;
        }

        final ItemStack finalStack = hatStack.copy();

        player.getCapability(HatDataCapability.HAT_DATA).ifPresent(data -> {
            if (!data.hasHat(hatId)) {

                // UNLOCK
                data.unlockHat(hatId);
                int totalUnlocked = data.getUnlockedHats().size();

                // 🔧 ADVANCEMENT PROGRESSION SYSTEM
                String advancementPath = null;

                if (totalUnlocked == 1) {
                    advancementPath = "hats/collect_1";    // Milestone 2: New Wardrobe
                } else if (totalUnlocked == 10) {
                    advancementPath = "hats/collect_10";   // Milestone 3: Hat Apprentice
                } else if (totalUnlocked == 100) {
                    advancementPath = "hats/collect_100";  // Milestone 4: Head Hunter
                } else if (totalUnlocked == 200) {
                    advancementPath = "hats/collect_200";  // Milestone 5: Elite Stylist
                } else if (totalUnlocked == 300) {
                    advancementPath = "hats/collect_300";  // Milestone 6: Hatter Extraordinary
                } else if (totalUnlocked == 332) {
                    advancementPath = "hats/collect_332";  // Milestone 7: The Master Hatter
                }

                if (advancementPath != null) {
                    Advancement adv = player.getServer().getAdvancements()
                            .getAdvancement(new ResourceLocation(AstryxionsHats.MODID, advancementPath));
                    if (adv != null) {
                        player.getAdvancements().award(adv, "unlock_via_code");
                    }
                }

                // NOTIFY & SYNC
                HatPacketHandler.sendToPlayer(player, new PacketHatUnlocked(finalStack));
                HatPacketHandler.sendToPlayer(player, new PacketSyncHat(data.serializeNBT()));

                // SAVE
                player.getInventory().setChanged();

                AstryxionsHats.LOGGER.info("Unlocked hat: {} | Total: {}", hatId, totalUnlocked);
            }
        });

        // 3. CLEANUP: Clear custom slot
        HatManager.clearHat(mob);
    }
}