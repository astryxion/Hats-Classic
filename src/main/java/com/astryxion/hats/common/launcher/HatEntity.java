package com.astryxion.hats.common.launcher;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.data.ServerHatUnlocks;
import com.astryxion.hats.common.hat.HatAdvancements;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketRehatify;
import com.astryxion.hats.common.network.PacketSetUnlockedHats;
import com.astryxion.hats.common.sound.HatsSounds;
import com.astryxion.hats.common.trade.TradeHats;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class HatEntity extends Entity {

    private static final EntityDataAccessor<String> HAT_ID =
            SynchedEntityData.defineId(HatEntity.class, EntityDataSerializers.STRING);

    @Nullable
    private UUID lastInteractedEntity;
    private boolean leftEntity;
    private int age;
    private int knockback;
    private boolean wasCollided;
    private boolean isRogue;

    public float lastRotX;
    public float lastRotY;
    public float rotX;
    public float rotY;
    public float rotFactorX;
    public float rotFactorY;

    public HatEntity(EntityType<? extends HatEntity> type, Level level) {
        super(type, level);
        rotFactorX = (random.nextFloat() * 2F - 1F) * 45F;
        rotFactorY = (random.nextFloat() * 2F - 1F) * 45F;
    }

    public HatEntity setHatId(ResourceLocation id) {
        entityData.set(HAT_ID, id == null ? "" : id.toString());
        return this;
    }

    @Nullable
    public ResourceLocation getHatId() {
        String raw = entityData.get(HAT_ID);
        return raw == null || raw.isEmpty() ? null : ResourceLocation.tryParse(raw);
    }

    public HatEntity setLastInteracted(LivingEntity living) {
        lastInteractedEntity = living.getUUID();
        leftEntity = false;
        age = 0;
        return this;
    }

    public void setKnockbackStrength(int value) {
        knockback = value;
    }

    public void setThrowableHeading(double motionX, double motionY, double motionZ, float magnitude, float recoil) {
        float length = Mth.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX = motionX / length + random.nextGaussian() * 0.0075D * recoil;
        motionY = motionY / length + random.nextGaussian() * 0.0075D * recoil;
        motionZ = motionZ / length + random.nextGaussian() * 0.0075D * recoil;
        setDeltaMovement(motionX * magnitude, motionY * magnitude, motionZ * magnitude);
        float flat = Mth.sqrt((float) (motionX * motionX + motionZ * motionZ));
        setYRot((float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI));
        setXRot((float) (Math.atan2(motionY, flat) * 180.0D / Math.PI));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    public void tick() {
        lastRotX = rotX;
        lastRotY = rotY;
        super.tick();
        if (isRemoved()) {
            return;
        }
        age++;
        if (age > Config.hatEntityLifespan && !level().isClientSide) {
            discard();
            return;
        }

        wasCollided = horizontalCollision || verticalCollision;
        float eye = getEyeHeight();
        if (isInWater() && getFluidHeight(FluidTags.WATER) > eye) {
            applyFloatMotion();
        } else if (isInLava() && getFluidHeight(FluidTags.LAVA) > eye) {
            applyLavaMotion();
        }

        if (!level().isClientSide) {
            List<Entity> list = level().getEntities(this, getBoundingBox(),
                    entity -> entity instanceof LivingEntity && !entity.isSpectator());
            if (!leftEntity) {
                leftEntity = true;
                for (Entity entity : list) {
                    if (entity.getUUID().equals(lastInteractedEntity)) {
                        leftEntity = false;
                        break;
                    }
                }
            }

            LivingEntity collided = null;
            for (Entity entity : list) {
                if (canPutOnHat(entity)) {
                    collided = (LivingEntity) entity;
                    break;
                }
            }
            if (collided == null) {
                Vec3 eyePos = position().add(0.0D, getEyeHeight(), 0.0D);
                EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                        level(), this, eyePos, eyePos.add(getDeltaMovement()),
                        getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0D),
                        this::canPutOnHat);
                if (hit != null && hit.getEntity() instanceof LivingEntity living) {
                    collided = living;
                }
            }
            if (collided != null) {
                putOn(collided);
                return;
            }
        }

        move(MoverType.SELF, getDeltaMovement());
        if (!wasCollided && (horizontalCollision || verticalCollision) && getHatId() != null) {
            if (!level().isClientSide) {
                level().playSound(null, getX(), getY(), getZ(),
                        HatsSounds.BONK.get(), SoundSource.NEUTRAL, 0.5F,
                        0.85F + (random.nextFloat() * 2F - 1F) * 0.075F);
                isRogue = false;
            } else {
                rotFactorX += (random.nextFloat() * 2F - 1F) * 45F;
                rotFactorY += (random.nextFloat() * 2F - 1F) * 45F;
            }
        }

        setDeltaMovement(getDeltaMovement().add(0.0D, -0.02D, 0.0D));
        if (onGround()) {
            setDeltaMovement(getDeltaMovement().multiply(0.8D, 0.8D, 0.8D));
            rotFactorX *= 0.8F;
            rotFactorY *= 0.8F;
        } else {
            setDeltaMovement(getDeltaMovement().multiply(0.98D, 0.98D, 0.98D));
            rotFactorX *= 0.98F;
            rotFactorY *= 0.98F;
        }
        rotX += rotFactorX;
        rotY += rotFactorY;
    }

    private void putOn(LivingEntity target) {
        ResourceLocation incoming = getHatId();
        if (incoming == null) {
            discard();
            return;
        }
        if (isOnFire()) {
            target.setSecondsOnFire(5);
        }
        if (knockback > 0) {
            Vec3 push = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(knockback * 0.6D);
            if (push.lengthSqr() > 0.0D) {
                target.push(push.x, knockback * 0.2D, push.z);
                knockback = 0;
                if (level() instanceof ServerLevel server) {
                    server.getChunkSource().broadcast(target, new ClientboundSetEntityMotionPacket(target));
                }
            }
        }

        ServerPlayer shooter = null;
        if (level() instanceof ServerLevel server && lastInteractedEntity != null) {
            shooter = server.getServer().getPlayerList().getPlayer(lastInteractedEntity);
        }
        if (isRogue && target instanceof ServerPlayer hit) {
            HatAdvancements.award(hit, "hats/rogue_hat");
        }
        if (!(target instanceof Player) && shooter != null) {
            HatAdvancements.award(shooter, "hats/change_mob_hat");
            if (target instanceof Pig && "iChun".equals(target.getName().getString())) {
                HatAdvancements.award(shooter, "hats/pig_chun_hat");
            }
        }

        ResourceLocation original = HatManager.getEquippedHatId(target);
        HatManager.setHat(target, TradeHats.item(incoming));
        HatPacketHandler.syncHatPartToTracking(target);
        if (level() instanceof ServerLevel) {
            for (ServerPlayer observer : ((ServerLevel) level()).getServer().getPlayerList().getPlayers()) {
                if (observer.level() == level() && observer.distanceToSqr(target) < 64 * 64) {
                    HatPacketHandler.sendToPlayer(observer, new PacketRehatify(target.getId()));
                }
            }
        }

        if (target instanceof ServerPlayer player) {
            if (HatLaunch.useInventory(player)) {
                HatLaunch.grant(player, incoming);
                if (!Config.hatLauncherDoesNotRemoveHatFromInventory && original != null) {
                    ServerHatUnlocks.get(player).take(player, original);
                }
                PacketSetUnlockedHats.send(player, ServerHatUnlocks.get(player).copy(player), incoming);
            }
        }

        if (original != null) {
            setLastInteracted(target);
            isRogue = true;
            setHatId(original);
            setDeltaMovement(
                    random.nextGaussian() * 0.2F,
                    0.2F + random.nextFloat() * 0.2F,
                    random.nextGaussian() * 0.2F);
            if (level() instanceof ServerLevel server) {
                server.getChunkSource().broadcast(this, new ClientboundSetEntityMotionPacket(this));
            }
        } else {
            discard();
        }
    }

    private boolean canPutOnHat(Entity entity) {
        return entity instanceof LivingEntity living
                && HatLaunch.canWear(living)
                && !(entity.getUUID().equals(lastInteractedEntity) && !leftEntity)
                && !(entity instanceof Player && !Config.hatLauncherReplacesPlayerHat);
    }

    private void applyFloatMotion() {
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(motion.x * 0.99F, motion.y + (motion.y < 0.06F ? 5.0E-4F : 0.0F), motion.z * 0.99F);
    }

    private void applyLavaMotion() {
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(motion.x * 0.95F, motion.y + (motion.y < 0.06F ? 5.0E-4F : 0.0F), motion.z * 0.95F);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(HAT_ID, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(HAT_ID, tag.getString("hatId"));
        if (tag.hasUUID("lastInteractedEntity")) {
            lastInteractedEntity = tag.getUUID("lastInteractedEntity");
        }
        leftEntity = tag.getBoolean("leftEntity");
        age = tag.getInt("age");
        isRogue = tag.getBoolean("isRogue");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("hatId", entityData.get(HAT_ID));
        if (lastInteractedEntity != null) {
            tag.putUUID("lastInteractedEntity", lastInteractedEntity);
        }
        tag.putBoolean("leftEntity", leftEntity);
        tag.putInt("age", age);
        tag.putBoolean("isRogue", isRogue);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64.0D * 64.0D;
    }
}
