package com.astryxion.hats.client.gui;

import com.astryxion.hats.client.render.ClientHatPreview;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatPart;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Hats Renewed {@code HatPreviewRenderer}: draw the real player wearing a
 * specific hat. Nametags stay hidden because this is the local player, same
 * as the legacy chooser.
 */
public final class HatPreviewRenderer {

    private HatPreviewRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, LivingEntity appearance, Item hat,
            int cellX, int cellY, int cellW, int cellH) {
        if (appearance == null || hat == null) {
            return;
        }
        render(graphics, appearance, new ItemStack(hat),
                cellX + cellW / 2, cellY + cellH + 54, 42,
                cellX + 1, cellY + 1, cellX + cellW - 1, cellY + cellH - 1);
    }

    public static void render(GuiGraphicsExtractor graphics, LivingEntity appearance, ItemStack hat,
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

        float bodyYaw = appearance.yBodyRot;
        float yRot = appearance.getYRot();
        float xRot = appearance.getXRot();
        float headO = appearance.yHeadRotO;
        float head = appearance.yHeadRot;
        boolean onGround = appearance.onGround();

        appearance.yBodyRot = 180f;
        appearance.setYRot(180f);
        appearance.setXRot(-8f);
        appearance.yHeadRot = 180f;
        appearance.yHeadRotO = 180f;
        appearance.setOnGround(true);

        try {
            graphics.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRenderer<? super LivingEntity, ?> renderer = dispatcher.getRenderer(appearance);
            EntityRenderState renderState = renderer.createRenderState(appearance, 1.0F);
            renderState.shadowPieces.clear();
            renderState.outlineColor = 0;
            if (renderState instanceof LivingEntityRenderState livingRenderState) {
                livingRenderState.boundingBoxWidth = livingRenderState.boundingBoxWidth / livingRenderState.scale;
                livingRenderState.boundingBoxHeight = livingRenderState.boundingBoxHeight / livingRenderState.scale;
                livingRenderState.scale = 1.0F;
                livingRenderState.walkAnimationPos = 0.0F;
                livingRenderState.walkAnimationSpeed = 0.0F;
            }
            Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf cameraAngle = new Quaternionf().rotateY((float) Math.toRadians(-25.0));
            Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F, 0.0F);
            float halfH = renderState.boundingBoxHeight * scale / 2.0F;
            float halfW = renderState.boundingBoxWidth * scale / 2.0F;
            int centerY = Math.round(footY - halfH);
            int pad = scale;
            int x0 = Math.round(centerX - halfW) - pad;
            int x1 = Math.round(centerX + halfW) + pad;
            int y0 = Math.round(centerY - halfH) - pad;
            int y1 = Math.round(centerY + halfH) + pad;
            graphics.entity(renderState, scale, translation, rotation, cameraAngle, x0, y0, x1, y1);
            graphics.disableScissor();
        } finally {
            appearance.yBodyRot = bodyYaw;
            appearance.setYRot(yRot);
            appearance.setXRot(xRot);
            appearance.yHeadRotO = headO;
            appearance.yHeadRot = head;
            appearance.setOnGround(onGround);

            ClientHatPreview.end();
            ClientHatPreview.clear(appearance.getUUID());
            if (part != null) {
                part.setHatStack(original);
            }
        }
    }
}
