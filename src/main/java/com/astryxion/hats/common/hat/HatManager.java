package com.astryxion.hats.common.hat;

import com.astryxion.hats.AstryxionsHats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class HatManager {

    private HatManager() {}

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
        return entity == null ? null : HatPartCapability.getOrCreate(entity);
    }

    public static ItemStack getHatStack(LivingEntity entity) {
        if (entity == null) return ItemStack.EMPTY;

        if (entity.isBaby()) {
            return ItemStack.EMPTY;
        }

        HatPart part = get(entity);
        ItemStack helmetStack = entity.getItemBySlot(EquipmentSlot.HEAD);

        if (isCustomOnly(entity)) {

            if (!(entity instanceof Player)) {
                if (!helmetStack.isEmpty() && isModHat(helmetStack)) {
                    if (part != null && (entity.level().isClientSide() || !part.hasHat())) {
                        part.setHatStack(helmetStack.copy());
                    }
                    entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }

            return (part != null && part.hasHat()) ? part.getHatStack() : ItemStack.EMPTY;
        }

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
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static boolean isModHat(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.getItem() instanceof ArmorItem armor) {
            if (armor.getEquipmentSlot() == EquipmentSlot.HEAD) {
                return false;
            }
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getNamespace().equals(AstryxionsHats.MODID);
    }
}
