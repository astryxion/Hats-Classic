package com.astryxion.hats.common.hat;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class HatManager {

    private HatManager() {}

    public static HatPart get(LivingEntity entity) {
        return entity.getCapability(HatPartCapability.HAT_PART).orElse(null);
    }

    /**
     * Hats live only in {@link HatPart}. The vanilla helmet slot is never a
     * courier — zombies keep their armor, piglins can loot helmets, and
     * undead still burn in daylight.
     */
    public static ItemStack getHatStack(LivingEntity entity) {
        if (entity == null) {
            return ItemStack.EMPTY;
        }
        HatPart part = get(entity);
        return (part != null && part.hasHat()) ? part.getHatStack() : ItemStack.EMPTY;
    }

    public static void setHatStack(LivingEntity entity, ItemStack stack) {
        if (entity == null) {
            return;
        }

        HatPart part = get(entity);
        if (part != null) {
            part.setHatStack(stack);
        }

        stripCourierHat(entity);

        if (!entity.level().isClientSide()) {
            HatPacketHandler.syncHatPartToTracking(entity);
        }
    }

    public static void clearHat(LivingEntity entity) {
        if (entity == null) {
            return;
        }

        HatPart part = get(entity);
        if (part != null) {
            part.clear();
        }

        stripCourierHat(entity);

        if (!entity.level().isClientSide()) {
            HatPacketHandler.syncHatPartToTracking(entity);
        }
    }

    /**
     * Old builds stuffed hats into {@code HEAD} for client sync. Pull that
     * stack into the custom slot if needed, then free the helmet.
     */
    public static void migrateAndStripCourierHat(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !isModHat(helmet)) {
            return;
        }

        HatPart part = get(entity);
        if (part != null && !part.hasHat()) {
            part.setHatStack(helmet.copy());
        }
        entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        HatPacketHandler.syncHatPartToTracking(entity);
    }

    private static void stripCourierHat(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
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
        if (stack.isEmpty()) {
            return null;
        }
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    /**
     * Checks if an item is from this mod and is NOT a standard armor helmet.
     */
    public static boolean isModHat(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof ArmorItem armor) {
            if (armor.getEquipmentSlot() == EquipmentSlot.HEAD) {
                return false;
            }
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.getNamespace().equals(Hats.MODID);
    }

    /**
     * Real helmet / pumpkin / skull in the vanilla head slot. Mod hats never
     * live there, so a non-empty non-hat stack is headgear the hat must clear.
     */
    public static boolean wearsHelmet(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        return !head.isEmpty() && !isModHat(head);
    }
}
