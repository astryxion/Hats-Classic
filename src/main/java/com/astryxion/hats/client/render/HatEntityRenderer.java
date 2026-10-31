package com.astryxion.hats.client.render;

import com.astryxion.hats.common.launcher.HatEntity;
import com.astryxion.hats.common.trade.TradeHats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HatEntityRenderer extends EntityRenderer<HatEntity, HatEntityRenderer.State> {

    public HatEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HatEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.rotY = Mth.lerp(partialTick, entity.lastRotY, entity.rotY);
        state.rotX = Mth.lerp(partialTick, entity.lastRotX, entity.rotX);
        Item item = TradeHats.item(entity.getHatId());
        state.hat = item == null ? ItemStack.EMPTY : new ItemStack(item);
        state.seed = entity.getId();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.hat.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.rotX));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        HatRenderLayer.submitHatItem(poseStack, collector, state.hat, state.lightCoords, OverlayTexture.NO_OVERLAY, state.seed);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public Identifier getTextureLocation(State state) {
        return Identifier.fromNamespaceAndPath("hats", "textures/model/hat_launcher.png");
    }

    public static class State extends EntityRenderState {
        public float rotY;
        public float rotX;
        public ItemStack hat = ItemStack.EMPTY;
        public int seed;
    }
}
