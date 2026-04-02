package com.astryxion.astryxionshats.mixin;

import com.astryxion.astryxionshats.common.hat.HatManager;
import com.astryxion.astryxionshats.common.spawn.HatSpawnHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void astryxionshats$tick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide()) return;

        HatSpawnHandler.onFirstTick(entity);

        if (!HatManager.isCustomOnly(entity)) return;
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty() && HatManager.isModHat(helmet)) {
            entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }
    }
}
