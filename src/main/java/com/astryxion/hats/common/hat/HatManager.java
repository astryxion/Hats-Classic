package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class HatManager {

    private HatManager() {}

    /**
     * Mobs that must use the custom slot capability.
     * Includes Players for capability access, but logic handles them differently.
     */
    public static boolean isCustomOnly(LivingEntity entity) {
        return entity instanceof Player
                || entity instanceof Zombie
                || entity instanceof ZombieVillager
                || entity instanceof Husk
                || entity instanceof Drowned
                || entity instanceof Skeleton
                || entity instanceof Stray
                || entity instanceof AbstractPiglin
                || entity instanceof ZombifiedPiglin;
    }

    public static HatPart get(LivingEntity entity) {
        return entity.getCapability(HatPartCapability.HAT_PART).orElse(null);
    }

    /**
     * Finds the hat stack and handles the "Courier" sync logic.
     */
    public static ItemStack getHatStack(LivingEntity entity) {
        if (entity == null) return ItemStack.EMPTY;

        // ============================
        // UNIVERSAL BABY BLOCKER
        // ============================

        // Players are never babies, so safe.
        if (entity.isBaby()) {
            return ItemStack.EMPTY;
        }

        HatPart part = get(entity);
        ItemStack helmetStack = entity.getItemBySlot(EquipmentSlot.HEAD);

        // --- CUSTOM ENTITY SYSTEM ---
        if (isCustomOnly(entity)) {

            // CRITICAL FIX: Only run the auto-wipe logic for MOBS.
            // Players should keep their helmet slot untouched to avoid deleting armor.
            if (!(entity instanceof Player)) {
                if (!helmetStack.isEmpty() && isModHat(helmetStack)) {
                    // Capture for Client or ensure Server has it in capability before wipe
                    if (part != null && (entity.level().isClientSide() || !part.hasHat())) {
                        part.setHatStack(helmetStack.copy());
                    }
                    // Wipe helmet for mobs to allow burning/looting
                    entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }

            // Always return capability for these entities (including Players)
            return (part != null && part.hasHat()) ? part.getHatStack() : ItemStack.EMPTY;
        }

        // --- FALLBACK FOR REGULAR MOBS (Cows, Pigs, etc.) ---
        if (part != null && part.hasHat()) {
            return part.getHatStack();
        }

        if (!helmetStack.isEmpty() && isModHat(helmetStack)) {
            return helmetStack;
        }

        return ItemStack.EMPTY;
    }

    public static void setHatStack(LivingEntity entity, ItemStack stack) {
        if (entity == null) return;

        HatPart part = get(entity);
        if (part != null) {
            part.setHatStack(stack);
        }

        // Only put in helmet for mobs as a sync courier.
        // For players, we don't want to mess with their head slot during setting.
        if (!(entity instanceof Player)) {
            entity.setItemSlot(EquipmentSlot.HEAD, stack.copy());
        }
    }

    public static void clearHat(LivingEntity entity) {
        if (entity == null) return;

        HatPart part = get(entity);
        if (part != null) {
            part.clear();
        }

        // Only clear the helmet slot if it contains a mod hat.
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (isModHat(head)) {
            entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }
    }

    public static void setHat(LivingEntity entity, Item item) {
        if (item == null) {
            clearHat(entity);
        } else {
            setHatStack(entity, new ItemStack(item));
        }
    }

    public static ItemStack getHat(LivingEntity entity) {
        return getHatStack(entity);
    }

    public static boolean hasHat(LivingEntity entity) {
        return !getHatStack(entity).isEmpty();
    }

    public static ResourceLocation getEquippedHatId(LivingEntity entity) {
        ItemStack stack = getHatStack(entity);
        if (stack.isEmpty()) return null;
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    /**
     * Checks if an item is from this mod and is NOT a standard armor helmet.
     */
    public static boolean isModHat(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.getItem() instanceof ArmorItem armor) {
            if (armor.getEquipmentSlot() == EquipmentSlot.HEAD) {
                return false;
            }
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.getNamespace().equals(Hats.MODID);
    }
}
