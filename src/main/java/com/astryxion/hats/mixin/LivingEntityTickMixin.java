package com.astryxion.hats.mixin;

import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.spawn.HatSpawnHandler;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void hats$tick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide()) {
            return;
        }

        HatSpawnHandler.onFirstTick(entity);
        HatManager.migrateAndStripCourierHat(entity);
    }
}
