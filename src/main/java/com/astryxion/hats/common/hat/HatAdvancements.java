package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.data.ServerHatUnlocks;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Code-awarded hat advancements. Collection stops at the real unlockable
 * hat count, using the 1.16.5 steps and then continuing to that total.
 */
public final class HatAdvancements {

    public static final int HAT_TOTAL = 545;

    private static final int[] COLLECT = {1, 10, 25, 50, 100, 200, 300, 400, 500, HAT_TOTAL};
    private static final int[] SAME_HAT = {10, 25, 50};

    private HatAdvancements() {}

    public static void award(ServerPlayer player, String path) {
        if (player == null || player.level().getServer() == null || path == null) {
            return;
        }
        AdvancementHolder advancement = player.level().getServer().getAdvancements()
                .get(Identifier.fromNamespaceAndPath(Hats.MODID, path));
        if (advancement != null) {
            player.getAdvancements().award(advancement, "unlock_via_code");
        }
    }

    public static void onUnlockCounts(ServerPlayer player, Identifier hatId) {
        if (player == null || hatId == null) {
            return;
        }
        ServerHatUnlocks store = ServerHatUnlocks.get(player);
        int copies = store.count(player, hatId);
        for (int milestone : SAME_HAT) {
            if (copies >= milestone) {
                award(player, "hats/hat_count_" + milestone);
            }
        }
        int total = store.copy(player).size();
        for (int milestone : COLLECT) {
            if (total >= milestone) {
                award(player, "hats/collect_" + milestone);
            }
        }
    }

    public static void onEquip(ServerPlayer player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) {
            return;
        }
        award(player, "hats/trendsetter");
        int accessories = HatAccessories.showing(stack).size();
        if (accessories >= 1) {
            award(player, "hats/wear_hat_with_accessory");
        }
        if (accessories >= 3) {
            award(player, "hats/hat_with_three_or_more_accessories");
        }
        if (!HatColor.isDefault(stack)) {
            award(player, "hats/colourise_hat");
        }
    }
}
