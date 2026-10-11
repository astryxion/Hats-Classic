package com.astryxion.hats.common.launcher;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.data.ServerHatUnlocks;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.hatstand.HatStandEntity;
import com.astryxion.hats.common.trade.TradeHats;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class HatLaunch {

    public static final String RANDOM_ID = ":random";
    public static final String NBT_HAT = "hats_hatId";

    private HatLaunch() {}

    public static boolean useInventory(Player player) {
        return Config.hatMode == HatMode.HUNTING && !player.getAbilities().instabuild;
    }

    public static boolean canWear(LivingEntity entity) {
        if (entity instanceof Player) {
            return true;
        }
        if (entity instanceof HatStandEntity) {
            return false;
        }
        return Config.canMobWearHats(entity.getType());
    }

    public static String storedId(ItemStack stack) {
        if (stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA)) {
            return RANDOM_ID;
        }
        String value = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getStringOr(NBT_HAT, "");
        return value.isEmpty() ? RANDOM_ID : value;
    }

    public static void storeId(ItemStack stack, String id) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(NBT_HAT, id == null || id.isEmpty() ? RANDOM_ID : id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Nullable
    public static Identifier resolve(ServerPlayer player, ItemStack launcher) {
        String stored = storedId(launcher);
        if (RANDOM_ID.equals(stored)) {
            return randomOwned(player);
        }
        Identifier id = Identifier.tryParse(stored);
        if (id == null || !TradeHats.isHat(id)) {
            return null;
        }
        if (!useInventory(player)) {
            return id;
        }
        return playerHasHat(player, id, true) ? id : null;
    }

    public static boolean playerHasHat(ServerPlayer player, Identifier id, boolean minusWearing) {
        if (!useInventory(player)) {
            return TradeHats.isHat(id);
        }
        int owned = ServerHatUnlocks.get(player).count(player, id);
        if (minusWearing && id.equals(TradeHats.equippedId(player))) {
            owned--;
        }
        return owned > 0;
    }

    @Nullable
    public static Identifier randomOwned(ServerPlayer player) {
        List<Identifier> source = new ArrayList<>();
        if (useInventory(player)) {
            Identifier wearing = TradeHats.equippedId(player);
            ServerHatUnlocks store = ServerHatUnlocks.get(player);
            for (Identifier id : store.copy(player)) {
                int available = store.count(player, id);
                if (id.equals(wearing)) {
                    available--;
                }
                if (available > 0) {
                    source.add(id);
                }
            }
        } else {
            for (var item : BuiltInRegistries.ITEM.stream().toList()) {
                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (TradeHats.isHat(id)) {
                    source.add(id);
                }
            }
        }
        if (source.isEmpty()) {
            return null;
        }
        return source.get(player.getRandom().nextInt(source.size()));
    }

    public static void consume(ServerPlayer player, Identifier id) {
        if (useInventory(player)) {
            ServerHatUnlocks.get(player).take(player, id);
        }
    }

    public static void grant(ServerPlayer player, Identifier id) {
        ServerHatUnlocks.get(player).add(player, id);
    }

    @Nullable
    public static Identifier hatId(LivingEntity entity) {
        return HatManager.getEquippedHatId(entity);
    }

    public static boolean clientHasLaunchable(Player player, @Nullable Identifier id) {
        if (!useInventory(player)) {
            return id == null || TradeHats.isHat(id);
        }
        if (id == null) {
            Identifier wearing = TradeHats.equippedId(player);
            for (Identifier owned : com.astryxion.hats.client.hat.ClientHatUnlocks.all()) {
                int available = com.astryxion.hats.client.hat.ClientHatUnlocks.count(owned);
                if (owned.equals(wearing)) {
                    available--;
                }
                if (available > 0) {
                    return true;
                }
            }
            return false;
        }
        int owned = com.astryxion.hats.client.hat.ClientHatUnlocks.count(id);
        if (id.equals(TradeHats.equippedId(player))) {
            owned--;
        }
        return owned > 0;
    }

    public static boolean clientLooksAtHatted(Player player) {
        HitResult look = player.pick(4.0D, 1.0F, false);
        LivingEntity target = null;
        if (look instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            target = living;
        } else {
            Vec3 eye = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle().scale(4.0D);
            var box = player.getBoundingBox().expandTowards(lookVec).inflate(1.0D);
            EntityHitResult hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                    player.level(), player, eye, eye.add(lookVec), box,
                    entity -> entity instanceof LivingEntity && entity.isPickable(), 0.0F);
            if (hit != null && hit.getEntity() instanceof LivingEntity living) {
                target = living;
            }
        }
        if (target == null || !canWear(target)) {
            return false;
        }
        if (target instanceof Player && !Config.hatLauncherReplacesPlayerHat) {
            return false;
        }
        return HatManager.getEquippedHatId(target) != null;
    }
}
