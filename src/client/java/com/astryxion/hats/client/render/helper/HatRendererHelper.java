package com.astryxion.hats.client.render.helper;

import com.astryxion.hats.client.render.helper.individual.PlayerHatHelper;
import com.astryxion.hats.client.render.helper.individual.CowHatHelper;
import com.astryxion.hats.client.render.helper.individual.SheepHatHelper;
import com.astryxion.hats.client.render.helper.individual.PigHatHelper;
import com.astryxion.hats.client.render.helper.individual.ChickenHatHelper;
import com.astryxion.hats.client.render.helper.individual.SquidHatHelper;
import com.astryxion.hats.client.render.helper.individual.GlowSquidHatHelper;

import com.astryxion.hats.client.render.helper.individual.ZombieHatHelper;
import com.astryxion.hats.client.render.helper.individual.ZombieVillagerHatHelper;
import com.astryxion.hats.client.render.helper.individual.HuskHatHelper;
import com.astryxion.hats.client.render.helper.individual.DrownedHatHelper;

import com.astryxion.hats.client.render.helper.individual.SkeletonHatHelper;
import com.astryxion.hats.client.render.helper.individual.StrayHatHelper;
import com.astryxion.hats.client.render.helper.individual.WitherSkeletonHatHelper;

import com.astryxion.hats.client.render.helper.individual.CreeperHatHelper;

import com.astryxion.hats.client.render.helper.individual.EndermanHatHelper;

import com.astryxion.hats.client.render.helper.individual.PillagerHatHelper;
import com.astryxion.hats.client.render.helper.individual.VindicatorHatHelper;
import com.astryxion.hats.client.render.helper.individual.EvokerHatHelper;

import com.astryxion.hats.client.render.helper.individual.VillagerHatHelper;
import com.astryxion.hats.client.render.helper.individual.WanderingTraderHatHelper;
import com.astryxion.hats.client.render.helper.individual.IronGolemHatHelper;

import com.astryxion.hats.client.render.helper.individual.PiglinHatHelper;
import com.astryxion.hats.client.render.helper.individual.PiglinBruteHatHelper;
import com.astryxion.hats.client.render.helper.individual.ZombifiedPiglinHatHelper;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

/**
 * Central hat render transform dispatcher
 * (iChun-style system)
 */
public class HatRendererHelper {

    public static void applyTransforms(
            LivingEntity entity,
            PoseStack poseStack,
            float partialTicks
    ) {

        // ============================
        // PLAYER
        // ============================

        if (entity instanceof Player) {
            PlayerHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        // ============================
        // ZOMBIE FAMILY
        // ============================

        if (entity instanceof ZombieVillager) {
            ZombieVillagerHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Zombie) {
            ZombieHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Husk) {
            HuskHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Drowned) {
            DrownedHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        // ============================
        // SKELETON FAMILY
        // ============================

        if (entity instanceof Skeleton) {
            SkeletonHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity.getType() == EntityType.STRAY) {
            StrayHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity.getType() == EntityType.WITHER_SKELETON) {
            WitherSkeletonHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        // ============================
        // CREEPER FAMILY
        // ============================

        if (entity instanceof Creeper) {
            CreeperHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        // ============================
        // ENDER
        // ============================

        if (entity instanceof EnderMan) {
            EndermanHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        // ============================
        // ILLAGER FAMILY
        // ============================

        if (entity instanceof Pillager) {
            PillagerHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Vindicator) {
            VindicatorHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Evoker) {
            EvokerHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        // ============================
        // VILLAGER FAMILY
        // ============================

        if (entity.getType() == EntityType.WANDERING_TRADER) {
            WanderingTraderHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Villager) {
            VillagerHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof IronGolem) {
            IronGolemHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        // ============================
        // PIGLIN FAMILY
        // ============================

        if (entity instanceof ZombifiedPiglin) {
            ZombifiedPiglinHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Piglin) {
            PiglinHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof PiglinBrute) {
            PiglinBruteHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        // ============================
        // PASSIVE MOBS
        // ============================

        if (entity instanceof Cow) {
            CowHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Sheep) {
            SheepHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Pig) {
            PigHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Chicken) {
            ChickenHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof GlowSquid) {
            GlowSquidHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof Squid) {
            SquidHatHelper.apply(entity, poseStack, partialTicks);
        }
    }
}
