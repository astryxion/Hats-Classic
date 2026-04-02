package com.astryxion.astryxionshats.client.render.helper;

import com.astryxion.astryxionshats.client.render.helper.individual.PlayerHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.CowHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.SheepHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.PigHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.ChickenHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.SquidHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.GlowSquidHatHelper;

import com.astryxion.astryxionshats.client.render.helper.individual.ZombieHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.ZombieVillagerHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.HuskHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.DrownedHatHelper;

import com.astryxion.astryxionshats.client.render.helper.individual.SkeletonHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.StrayHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.WitherSkeletonHatHelper;

import com.astryxion.astryxionshats.client.render.helper.individual.CreeperHatHelper;

import com.astryxion.astryxionshats.client.render.helper.individual.EndermanHatHelper;

import com.astryxion.astryxionshats.client.render.helper.individual.PillagerHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.VindicatorHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.EvokerHatHelper;

import com.astryxion.astryxionshats.client.render.helper.individual.VillagerHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.WanderingTraderHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.IronGolemHatHelper;

import com.astryxion.astryxionshats.client.render.helper.individual.PiglinHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.PiglinBruteHatHelper;
import com.astryxion.astryxionshats.client.render.helper.individual.ZombifiedPiglinHatHelper;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Squid;

import net.minecraft.world.entity.GlowSquid;

import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Drowned;

import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;

import net.minecraft.world.entity.monster.Creeper;

import net.minecraft.world.entity.monster.EnderMan;

import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Evoker;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.animal.IronGolem;

import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;

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

        if (entity instanceof Stray) {
            StrayHatHelper.apply(entity, poseStack, partialTicks);
            return;
        }

        if (entity instanceof WitherSkeleton) {
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

        if (entity instanceof WanderingTrader) {
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
