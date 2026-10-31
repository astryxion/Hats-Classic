package com.astryxion.hats.client.render;

import com.astryxion.hats.Hats;
import com.astryxion.hats.client.tabula.TabulaHatPlacement;
import com.astryxion.hats.client.tabula.TabulaHatRenderer;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hatstand.HatStandEntity;
import com.astryxion.hats.common.tabula.TabulaHats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class HatStandRenderer extends EntityRenderer<HatStandEntity, HatStandRenderer.State> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Hats.MODID, "hat_stand"), "main");
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Hats.MODID, "textures/entity/hat_stand.png");

    private final HatStandModel model;

    public HatStandRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new HatStandModel(context.bakeLayer(LAYER));
        shadowRadius = 0.3F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HatStandEntity stand, State state, float partialTick) {
        super.extractRenderState(stand, state, partialTick);
        state.yRot = Mth.rotLerp(partialTick, stand.yRotO, stand.getYRot());
        state.hat = HatManager.getHat(stand).copy();
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        stack.pushPose();
        stack.translate(0, 1.5D, 0);
        stack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        stack.scale(-1, -1, 1);
        model.setupAnim(state);
        collector.submitModel(
                model,
                state,
                stack,
                RenderTypes.entityCutout(TEXTURE),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );

        ItemStack hat = state.hat;
        if (!hat.isEmpty()) {
            stack.pushPose();
            model.translateToHead(stack);
            if (TabulaHats.isTabula(hat)) {
                float geometryScale = TabulaHatPlacement.applyGeometryOffset(stack, model.getHead());
                TabulaHatPlacement.applyZFight(stack);
                TabulaHatRenderer.render(stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, hat, geometryScale);
            } else {
                stack.scale(0.875F, 0.875F, 0.875F);
                stack.translate(0, -0.25F, 0);
                stack.mulPose(Axis.YP.rotationDegrees(180));
                stack.scale(0.625F, -0.625F, -0.625F);
                HatRenderLayer.submitHatItem(stack, collector, hat, state.lightCoords, OverlayTexture.NO_OVERLAY);
            }
            stack.popPose();
        }
        stack.popPose();
        super.submit(state, stack, collector, camera);
    }

    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    public static class State extends EntityRenderState {
        public float yRot;
        public ItemStack hat = ItemStack.EMPTY;
    }
}
