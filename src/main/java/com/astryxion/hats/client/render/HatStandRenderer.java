package com.astryxion.hats.client.render;

import com.astryxion.hats.Hats;
import com.astryxion.hats.client.tabula.TabulaHatPlacement;
import com.astryxion.hats.client.tabula.TabulaHatRenderer;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatColor;
import com.astryxion.hats.common.hatstand.HatStandEntity;
import com.astryxion.hats.common.tabula.TabulaHats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HatStandRenderer extends EntityRenderer<HatStandEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(Hats.MODID, "hat_stand"), "main");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Hats.MODID, "textures/entity/hat_stand.png");

    private final HatStandModel model;

    public HatStandRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new HatStandModel(context.bakeLayer(LAYER));
        shadowRadius = 0.3F;
    }

    @Override
    public void render(HatStandEntity stand, float entityYaw, float partialTick,
            PoseStack stack, MultiBufferSource buffers, int light) {
        stack.pushPose();
        stack.translate(0, 1.5D, 0);
        stack.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partialTick, stand.yRotO, stand.getYRot())));
        stack.scale(-1, -1, 1);
        model.setupAnim(stand, 0, 0, stand.tickCount + partialTick, 0, 0);
        model.renderToBuffer(
                stack,
                buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                light,
                OverlayTexture.NO_OVERLAY,
                1, 1, 1, 1
        );

        ItemStack hat = HatManager.getHat(stand);
        if (!hat.isEmpty()) {
            stack.pushPose();
            model.translateToHead(stack);
            if (TabulaHats.isTabula(hat)) {
                float geometryScale = TabulaHatPlacement.applyGeometryOffset(stack, model.getHead());
                TabulaHatPlacement.applyZFight(stack);
                TabulaHatRenderer.render(stack, buffers, light, OverlayTexture.NO_OVERLAY, hat, geometryScale);
            } else {
                stack.scale(0.875F, 0.875F, 0.875F);
                stack.translate(0, -0.25F, 0);
                stack.mulPose(Axis.YP.rotationDegrees(180));
                stack.scale(0.625F, -0.625F, -0.625F);
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        stand, hat, ItemDisplayContext.HEAD, false, stack, alphaBuffer(buffers, HatColor.alpha(hat)),
                        stand.level(), light, OverlayTexture.NO_OVERLAY, stand.getId()
                );
            }
            stack.popPose();
        }
        stack.popPose();
        super.render(stand, entityYaw, partialTick, stack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(HatStandEntity entity) {
        return TEXTURE;
    }

    private static MultiBufferSource alphaBuffer(MultiBufferSource source, int alpha) {
        if (alpha >= 255) {
            return source;
        }
        float multiplier = Math.max(0, alpha) / 255.0F;
        return ignored -> new AlphaVertexConsumer(source.getBuffer(Sheets.translucentItemSheet()), multiplier);
    }

    private static final class AlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alpha;

        private AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = alpha;
        }

        @Override public VertexConsumer vertex(double x, double y, double z) { delegate.vertex(x, y, z); return this; }
        @Override public VertexConsumer color(int r, int g, int b, int a) { delegate.color(r, g, b, Math.round(a * alpha)); return this; }
        @Override public VertexConsumer uv(float u, float v) { delegate.uv(u, v); return this; }
        @Override public VertexConsumer overlayCoords(int u, int v) { delegate.overlayCoords(u, v); return this; }
        @Override public VertexConsumer uv2(int u, int v) { delegate.uv2(u, v); return this; }
        @Override public VertexConsumer normal(float x, float y, float z) { delegate.normal(x, y, z); return this; }
        @Override public void endVertex() { delegate.endVertex(); }
        @Override public void defaultColor(int r, int g, int b, int a) { delegate.defaultColor(r, g, b, Math.round(a * alpha)); }
        @Override public void unsetDefaultColor() { delegate.unsetDefaultColor(); }
    }
}
