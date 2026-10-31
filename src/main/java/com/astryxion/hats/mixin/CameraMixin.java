package com.astryxion.hats.mixin;

import com.astryxion.hats.client.gui.modern.ModernGuiCamera;
import com.astryxion.hats.client.gui.HatsGui;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    protected abstract void setPosition(Vec3 pos);

    @Inject(method = "setup", at = @At("TAIL"))
    private void hats$orbitForModernGui(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (!detached) {
            return;
        }

        float progress = ModernGuiCamera.progress(partialTick);
        if (progress <= 0f) {
            return;
        }

        Entity preview = HatsGui.getPreviewEntity();
        if (preview != null) {
            entity = preview;
        }

        float playerYaw = entity.getViewYRot(partialTick);
        float camYaw = playerYaw + 180f + ModernGuiCamera.guiYaw;
        float camPitch = Mth.clamp(Mth.lerp(progress, 0f, ModernGuiCamera.guiPitch), -85f, 85f);
        float camDist = progress * Mth.clamp(1.8f - ModernGuiCamera.guiZoom, 0.2f, 1.8f);
        float lateralOff = progress * 2.0f + ModernGuiCamera.guiPanX;

        float yawRad = camYaw * (float) (Math.PI / 180.0);
        float pitchRad = camPitch * (float) (Math.PI / 180.0);
        float cosPitch = Mth.cos(pitchRad);
        float sinYaw = Mth.sin(yawRad);
        float cosYaw = Mth.cos(yawRad);
        float sinPitch = Mth.sin(pitchRad);

        float fwdX = -sinYaw * cosPitch;
        float fwdY = -sinPitch;
        float fwdZ = cosYaw * cosPitch;

        double eyeX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double eyeY = Mth.lerp(partialTick, entity.yo, entity.getY()) + entity.getEyeHeight();
        double eyeZ = Mth.lerp(partialTick, entity.zo, entity.getZ());

        setRotation(camYaw, camPitch);
        setPosition(new Vec3(
                eyeX - fwdX * camDist - cosYaw * lateralOff,
                eyeY - fwdY * camDist + ModernGuiCamera.guiPanY,
                eyeZ - fwdZ * camDist - sinYaw * lateralOff
        ));
    }
}
