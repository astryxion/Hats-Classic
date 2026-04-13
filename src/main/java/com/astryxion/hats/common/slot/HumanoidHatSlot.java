package com.astryxion.hats.common.slot;

import com.astryxion.hats.common.hat.HatManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Invisible humanoid hat slot (iChun-style)
 * Not tied to armor or curios
 */
public class HumanoidHatSlot {

    /**
     * Can this entity use the humanoid hat slot?
     */
    public static boolean canUse(LivingEntity entity) {

        return entity instanceof Player
                || entity.getType().getCategory().isFriendly()
                || entity.getType().getCategory().isPersistent();
    }

    /**
     * Equip a hat
     */
    public static void equip(LivingEntity entity, ItemStack stack) {

        if (!canUse(entity))
            return;

        HatManager.setHatStack(entity, stack.copy());
    }

    /**
     * Unequip hat
     */
    public static void unequip(LivingEntity entity) {

        HatManager.clearHat(entity);
    }

    /**
     * Get current hat
     */
    public static ItemStack get(LivingEntity entity) {

        return HatManager.getHat(entity);
    }

    /**
     * Does entity have a hat equipped?
     */
    public static boolean has(LivingEntity entity) {

        return HatManager.hasHat(entity);
    }
}
