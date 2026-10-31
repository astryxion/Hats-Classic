package com.astryxion.hats.client.gui;

import com.astryxion.hats.client.render.ClientHatPreview;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatPart;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.joml.Quaternionf;

/**
 * Hats Renewed {@code HatPreviewRenderer}: draw the real player wearing a
 * specific hat. Nametags stay hidden because this is the local player, same
 * as the legacy chooser.
 */
public final class HatPreviewRenderer {

    private HatPreviewRenderer() {}

    public static void render(GuiGraphics graphics, LivingEntity appearance, Item hat,
            int cellX, int cellY, int cellW, int cellH) {
        if (appearance == null || hat == null) {
            return;
        }
        render(graphics, appearance, new ItemStack(hat),
                cellX + cellW / 2, cellY + cellH + 54, 42,
                cellX + 1, cellY + 1, cellX + cellW - 1, cellY + cellH - 1);
    }

    public static void render(GuiGraphics graphics, LivingEntity appearance, ItemStack hat,
            int centerX, int footY, int scale,
            int scissorX1, int scissorY1, int scissorX2, int scissorY2) {
        if (appearance == null || hat == null) {
            return;
        }

        HatPart part = HatManager.get(appearance);
        ItemStack original = part != null ? part.getHatStack().copy() : ItemStack.EMPTY;
        if (part != null) {
            part.setHatStack(hat);
        }
        ClientHatPreview.set(appearance.getUUID(), hat);
        ClientHatPreview.begin(hat);

        Quaternionf camera = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf pose = new Quaternionf().rotateY((float) Math.toRadians(-25.0));

        float bodyYaw = appearance.yBodyRot;
        float yRot = appearance.getYRot();
        float xRot = appearance.getXRot();
        float headO = appearance.yHeadRotO;
        float head = appearance.yHeadRot;
        float walk = appearance.walkDist;
        float walkO = appearance.walkDistO;
        boolean onGround = appearance.onGround();

        appearance.yBodyRot = 180f;
        appearance.setYRot(180f);
        appearance.setXRot(-8f);
        appearance.yHeadRot = 180f;
        appearance.yHeadRotO = 180f;
        appearance.walkDist = 0f;
        appearance.walkDistO = 0f;
        appearance.setOnGround(true);

        try {
            graphics.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);
            // 1.21's InventoryScreen helper adds a half-height shift and an extra
            // camera yaw. Those move the body out of the cell, so keep the 1.20.1 pose.
            graphics.pose().pushPose();
            graphics.pose().translate((float) centerX, (float) footY, 50.0F);
            // PoseStack.scale keeps unit normals. A raw scale matrix shrinks them and the
            // entity light math does not renormalize, which leaves the preview in shadow.
            graphics.pose().scale((float) scale, (float) scale, (float) -scale);
            graphics.pose().mulPose(camera);
            Lighting.setupForEntityInInventory();
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            dispatcher.overrideCameraOrientation(new Quaternionf(pose).conjugate());
            dispatcher.setRenderShadow(false);
            RenderSystem.runAsFancy(() -> dispatcher.render(
                    appearance, 0.0, 0.0, 0.0, 0.0F, 1.0F, graphics.pose(), graphics.bufferSource(), 15728880));
            graphics.flush();
            dispatcher.setRenderShadow(true);
            graphics.pose().popPose();
            Lighting.setupFor3DItems();
            graphics.disableScissor();
        } finally {
            appearance.yBodyRot = bodyYaw;
            appearance.setYRot(yRot);
            appearance.setXRot(xRot);
            appearance.yHeadRotO = headO;
            appearance.yHeadRot = head;
            appearance.walkDist = walk;
            appearance.walkDistO = walkO;
            appearance.setOnGround(onGround);

            ClientHatPreview.end();
            ClientHatPreview.clear(appearance.getUUID());
            if (part != null) {
                part.setHatStack(original);
            }
        }
    }
}
