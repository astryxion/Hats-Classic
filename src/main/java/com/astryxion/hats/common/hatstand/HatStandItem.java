package com.astryxion.hats.common.hatstand;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;

public class HatStandItem extends Item {

    public HatStandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        AABB bounds = HatStandRegistry.HAT_STAND_ENTITY.get().getDimensions()
                .makeBoundingBox(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        if (!level.noCollision(bounds) || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) {
            return InteractionResult.FAIL;
        }

        HatStandEntity stand = HatStandRegistry.HAT_STAND_ENTITY.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (stand == null) {
            return InteractionResult.FAIL;
        }
        float yaw = Mth.floor((Mth.wrapDegrees(context.getRotation() + 180.0F) + 22.5F) / 45.0F) * 45.0F;
        stand.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, yaw, 0.0F);
        level.addFreshEntity(stand);
        level.playSound(null, stand.blockPosition(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 1.0F);
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
