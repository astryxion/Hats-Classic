package com.astryxion.hats.common.hatstand;

import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketOpenHatStand;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.PacketDistributor;

public class HatStandEntity extends LivingEntity {

    public HatStandEntity(EntityType<? extends HatStandEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public HatStandEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(HatStandRegistry.HAT_STAND_ENTITY.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            HatPacketHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new PacketOpenHatStand(getId())
            );
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide() || isRemoved()) {
            return true;
        }
        boolean creative = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
        if (!creative) {
            spawnAtLocation(HatStandRegistry.HAT_STAND_ITEM.get());
            ItemStack hat = HatManager.getHat(this).copy();
            if (!hat.isEmpty()) {
                spawnAtLocation(hat);
            }
        }
        discard();
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0, 0, 0);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return java.util.List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldBeSaved() {
        return true;
    }

    @Override
    public void checkDespawn() {
    }
}
