package com.astryxion.hats.mixin;

import com.astryxion.hats.common.hat.HatManager;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla draws HEAD-slot items from the living render state's head item,
 * which {@code CustomHeadLayer} submits. HatRenderLayer submits the hat itself,
 * so clearing that state drops the duplicate villager/illager hat without hiding ours.
 */
@Mixin(LivingEntityRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("RETURN")
    )
    private void hats$skipModHatHeadItems(
            LivingEntity entity,
            LivingEntityRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (HatManager.isModHat(stack)) {
            state.headItem.clear();
        }
    }
}
