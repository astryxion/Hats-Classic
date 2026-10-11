package com.astryxion.hats.common.launcher;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketRehatify;
import com.astryxion.hats.common.network.PacketSetUnlockedHats;
import com.astryxion.hats.common.sound.HatsSounds;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import org.jetbrains.annotations.Nullable;

public class HatLauncherItem extends Item {

    public HatLauncherItem(Properties properties) {
        super(properties.enchantable(22));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            if (player.isShiftKeyDown() || shouldOpenSelect(player, stack)) {
                if (FMLEnvironment.getDist() == Dist.CLIENT) {
                    com.astryxion.hats.client.gui.HatLaunchSelectScreen.open(hand);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        var hatId = HatLaunch.resolve(serverPlayer, stack);
        if (hatId != null) {
            fire(serverPlayer, stack, hatId, hand);
        } else if (!tryStripLookedAt(serverPlayer, hand)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.6F,
                    0.9F + (player.getRandom().nextFloat() * 2F - 1F) * 0.075F);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof ServerPlayer player) {
            stripHat(player, target);
        }
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.POWER)
                || enchantment.is(Enchantments.PUNCH)
                || enchantment.is(Enchantments.FLAME);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    private static boolean shouldOpenSelect(Player player, ItemStack stack) {
        if (HatLaunch.clientLooksAtHatted(player)) {
            return false;
        }
        String stored = HatLaunch.storedId(stack);
        if (HatLaunch.RANDOM_ID.equals(stored)) {
            return !HatLaunch.clientHasLaunchable(player, null);
        }
        var id = net.minecraft.resources.Identifier.tryParse(stored);
        return !HatLaunch.clientHasLaunchable(player, id);
    }

    private static void fire(ServerPlayer player, ItemStack launcher, net.minecraft.resources.Identifier hatId, InteractionHand hand) {
        Level level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                HatsSounds.TUBE.get(), SoundSource.PLAYERS, 1.0F,
                0.9F + (player.getRandom().nextFloat() * 2F - 1F) * 0.075F);

        HatEntity hat = new HatEntity(HatLauncherRegistry.HAT_ENTITY.get(), level);
        hat.setHatId(hatId);
        hat.setLastInteracted(player);

        double pX = player.getX();
        double pZ = player.getZ();
        float yaw = player.getYRot() + (hand == InteractionHand.MAIN_HAND ? 90.0F : -90.0F);
        Vec3 side = Vec3.directionFromRotation(0.0F, yaw);
        pX += side.x * 0.25F;
        pZ += side.z * 0.25F;
        hat.setPos(pX, player.getY() + player.getEyeHeight() - 0.15F, pZ);
        hat.setYRot(player.getYRot());
        hat.setXRot(player.getXRot());

        int punch = enchantLevel(player, launcher, Enchantments.PUNCH);
        if (punch > 0) {
            hat.setKnockbackStrength(punch);
        }
        float momentum = 0.5F;
        int power = enchantLevel(player, launcher, Enchantments.POWER);
        if (power > 0) {
            momentum += power * 0.25F;
        }
        if (enchantLevel(player, launcher, Enchantments.FLAME) > 0) {
            hat.igniteForSeconds(5);
        }
        hat.setThrowableHeading(
                -Mth.sin(player.getYRot() / 180.0F * (float) Math.PI) * Mth.cos(player.getXRot() / 180.0F * (float) Math.PI),
                -Mth.sin(player.getXRot() / 180.0F * (float) Math.PI),
                Mth.cos(player.getYRot() / 180.0F * (float) Math.PI) * Mth.cos(player.getXRot() / 180.0F * (float) Math.PI),
                momentum,
                0.4F);
        level.addFreshEntity(hat);
        HatLaunch.consume(player, hatId);
        PacketSetUnlockedHats.send(player);
    }

    private static boolean tryStripLookedAt(ServerPlayer player, InteractionHand hand) {
        HitResult look = player.pick(4.0D, 1.0F, false);
        if (look.getType() != HitResult.Type.ENTITY || !(look instanceof EntityHitResult entityHit)) {
            Entity looked = nearestLookedLiving(player, 4.0D);
            if (looked instanceof LivingEntity living) {
                return stripHat(player, living);
            }
            return false;
        }
        if (entityHit.getEntity() instanceof LivingEntity living) {
            player.swing(hand, true);
            return stripHat(player, living);
        }
        return false;
    }

    @Nullable
    private static Entity nearestLookedLiving(Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().scale(range);
        var box = player.getBoundingBox().expandTowards(look).inflate(1.0D);
        EntityHitResult hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player.level(), player, eye, eye.add(look), box,
                entity -> entity instanceof LivingEntity && entity.isPickable(), 0.0F);
        return hit == null ? null : hit.getEntity();
    }

    private static boolean stripHat(ServerPlayer player, LivingEntity target) {
        var hatId = HatManager.getEquippedHatId(target);
        if (hatId == null) {
            return false;
        }
        if (target instanceof Player && !Config.hatLauncherReplacesPlayerHat) {
            return false;
        }
        Level level = target.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                HatsSounds.TUBE.get(), SoundSource.PLAYERS, 1.0F,
                0.9F + (player.getRandom().nextFloat() * 2F - 1F) * 0.075F);
        HatEntity hat = new HatEntity(HatLauncherRegistry.HAT_ENTITY.get(), level);
        hat.setHatId(hatId);
        hat.setLastInteracted(target);
        hat.setPos(target.getX(), target.getY() + target.getEyeHeight() - 0.15F, target.getZ());
        hat.setDeltaMovement(
                target.getRandom().nextGaussian() * 0.2F,
                0.2F + target.getRandom().nextFloat() * 0.2F,
                target.getRandom().nextGaussian() * 0.2F);
        level.addFreshEntity(hat);
        HatManager.clearHat(target);
        HatPacketHandler.syncHatPartToTracking(target);
        HatPacketHandler.sendToPlayer(player, new PacketRehatify(target.getId()));
        return true;
    }

    private static int enchantLevel(ServerPlayer player, ItemStack stack, ResourceKey<Enchantment> key) {
        return player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(key)
                .map(holder -> stack.getEnchantments().getLevel(holder))
                .orElse(0);
    }
}
