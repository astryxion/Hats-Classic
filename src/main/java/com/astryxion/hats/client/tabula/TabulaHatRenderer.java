package com.astryxion.hats.client.tabula;

import com.astryxion.hats.common.hat.HatAccessories;
import com.astryxion.hats.common.hat.HatColor;
import com.astryxion.hats.common.hat.HatGlint;
import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHatModelData;
import com.astryxion.hats.common.tabula.TabulaHats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public final class TabulaHatRenderer {

    private TabulaHatRenderer() {}

    public static void render(PoseStack poseStack, MultiBufferSource buffers, int light, int overlay,
            ItemStack stack, float scale) {
        TabulaHatDefinition def = TabulaHats.from(stack);
        if (def == null) {
            return;
        }

        int color = HatColor.get(stack);
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = HatColor.alpha(stack) / 255.0F;
        float[] rgba = {r, g, b, a};
        Set<String> showing = new HashSet<>(HatAccessories.showing(stack));
        boolean foil = HatGlint.get(stack);

        poseStack.pushPose();
        if (scale != 1f) {
            poseStack.scale(scale, scale, scale);
        }
        renderTree(poseStack, buffers, light, overlay, def, showing, rgba, foil);
        poseStack.popPose();
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay,
            ItemStack stack, float scale) {
        TabulaHatDefinition def = TabulaHats.from(stack);
        if (def == null) {
            return;
        }

        int color = HatColor.get(stack);
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = HatColor.alpha(stack) / 255.0F;
        float[] rgba = {r, g, b, a};
        Set<String> showing = new HashSet<>(HatAccessories.showing(stack));
        boolean foil = HatGlint.get(stack);

        poseStack.pushPose();
        if (scale != 1f) {
            poseStack.scale(scale, scale, scale);
        }
        renderTree(poseStack, collector, light, overlay, def, showing, rgba, foil);
        poseStack.popPose();
    }

    private static void renderTree(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay,
            TabulaHatDefinition def,
            Set<String> showing,
            float[] rgba,
            boolean foil
    ) {
        Set<String> hidden = new HashSet<>();
        for (TabulaHatDefinition accessory : def.accessories) {
            if (showing.contains(accessory.id)) {
                hidden.addAll(accessory.hideParentParts);
            }
        }
        renderModel(poseStack, buffers, light, overlay, def, rgba, hidden, foil);

        for (TabulaHatDefinition accessory : def.accessories) {
            if (showing.contains(accessory.id)) {
                renderTree(poseStack, buffers, light, overlay, accessory, showing, rgba, foil);
            }
        }
    }

    private static void renderTree(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay,
            TabulaHatDefinition def,
            Set<String> showing,
            float[] rgba,
            boolean foil
    ) {
        Set<String> hidden = new HashSet<>();
        for (TabulaHatDefinition accessory : def.accessories) {
            if (showing.contains(accessory.id)) {
                hidden.addAll(accessory.hideParentParts);
            }
        }
        renderModel(poseStack, collector, light, overlay, def, rgba, hidden, foil);

        for (TabulaHatDefinition accessory : def.accessories) {
            if (showing.contains(accessory.id)) {
                renderTree(poseStack, collector, light, overlay, accessory, showing, rgba, foil);
            }
        }
    }

    private static void renderModel(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay,
            TabulaHatDefinition def,
            float[] rgba,
            Set<String> hiddenParts,
            boolean foil
    ) {
        TabulaHatModelData model = def.modelData();
        Identifier texture = TabulaHatTextureManager.getTexture(def);
        if (model == null || texture == null) {
            return;
        }

        boolean cull = def.cull != null && def.cull;
        RenderType renderType = cull
                ? TabulaHatRenderTypes.hatTranslucentCull(texture)
                : TabulaHatRenderTypes.hatTranslucent(texture);
        VertexConsumer consumer = ItemFeatureRenderer.getFoilBuffer(buffers, renderType, false, foil);

        for (TabulaHatModelData.Bone root : model.roots) {
            renderBone(root, poseStack, consumer, light, overlay, rgba,
                    model.textureWidth, model.textureHeight, hiddenParts);
        }
    }

    private static void renderModel(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay,
            TabulaHatDefinition def,
            float[] rgba,
            Set<String> hiddenParts,
            boolean foil
    ) {
        TabulaHatModelData model = def.modelData();
        Identifier texture = TabulaHatTextureManager.getTexture(def);
        if (model == null || texture == null) {
            return;
        }
        boolean cull = def.cull != null && def.cull;
        RenderType renderType = cull
                ? TabulaHatRenderTypes.hatTranslucentCull(texture)
                : TabulaHatRenderTypes.hatTranslucent(texture);
        submitBones(collector, poseStack, renderType, foil, model, hiddenParts, rgba, light, overlay);
    }

    public static void renderModelData(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay,
            TabulaHatModelData model,
            Identifier texture,
            Set<String> hiddenParts,
            boolean foil
    ) {
        if (model == null || texture == null) {
            return;
        }
        float[] rgba = {1.0F, 1.0F, 1.0F, 1.0F};
        VertexConsumer consumer = ItemFeatureRenderer.getFoilBuffer(
                buffers, TabulaHatRenderTypes.hatTranslucentCull(texture), false, foil);
        Set<String> hidden = hiddenParts == null ? Set.of() : hiddenParts;
        for (TabulaHatModelData.Bone root : model.roots) {
            renderBone(root, poseStack, consumer, light, overlay, rgba,
                    model.textureWidth, model.textureHeight, hidden);
        }
    }

    public static void renderModelData(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay,
            TabulaHatModelData model,
            Identifier texture,
            Set<String> hiddenParts,
            boolean foil
    ) {
        if (model == null || texture == null) {
            return;
        }
        float[] rgba = {1.0F, 1.0F, 1.0F, 1.0F};
        Set<String> hidden = hiddenParts == null ? Set.of() : hiddenParts;
        submitBones(collector, poseStack, TabulaHatRenderTypes.hatTranslucentCull(texture), foil, model, hidden, rgba, light, overlay);
    }

    private static void submitBones(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            RenderType renderType,
            boolean foil,
            TabulaHatModelData model,
            Set<String> hidden,
            float[] rgba,
            int light,
            int overlay
    ) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) ->
                drawRoots(pose, consumer, model, hidden, rgba, light, overlay));
        if (foil) {
            collector.submitCustomGeometry(poseStack, RenderTypes.entityGlint(), (pose, consumer) ->
                    drawRoots(pose, consumer, model, hidden, rgba, light, overlay));
        }
    }

    private static void drawRoots(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            TabulaHatModelData model,
            Set<String> hidden,
            float[] rgba,
            int light,
            int overlay
    ) {
        PoseStack local = new PoseStack();
        local.last().set(pose);
        for (TabulaHatModelData.Bone root : model.roots) {
            renderBone(root, local, consumer, light, overlay, rgba, model.textureWidth, model.textureHeight, hidden);
        }
    }

    private static void renderBone(TabulaHatModelData.Bone bone, PoseStack poseStack, VertexConsumer consumer,
            int light, int overlay, float[] rgba, int texW, int texH, Set<String> hiddenParts) {
        if (!bone.visible || hiddenParts.contains(bone.name)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(bone.pivotX / 16f, bone.pivotY / 16f, bone.pivotZ / 16f);
        poseStack.mulPose(Axis.ZP.rotation(bone.rotZ));
        poseStack.mulPose(Axis.YP.rotation(bone.rotY));
        poseStack.mulPose(Axis.XP.rotation(bone.rotX));

        if (!bone.cubes.isEmpty()) {
            int boneTexW = bone.texW > 0 ? bone.texW : texW;
            int boneTexH = bone.texH > 0 ? bone.texH : texH;
            for (TabulaHatModelData.Cube cube : bone.cubes) {
                renderCube(cube, poseStack, consumer, light, overlay, rgba, bone.mirror || cube.mirror, boneTexW, boneTexH);
            }
        }

        for (TabulaHatModelData.Bone child : bone.children) {
            renderBone(child, poseStack, consumer, light, overlay, rgba, texW, texH, hiddenParts);
        }

        poseStack.popPose();
    }

    private static void renderCube(TabulaHatModelData.Cube cube, PoseStack poseStack, VertexConsumer consumer,
            int light, int overlay, float[] rgba, boolean mirror, int texW, int texH) {
        float ox = cube.originX / 16f;
        float oy = cube.originY / 16f;
        float oz = cube.originZ / 16f;
        float sx = cube.sizeX / 16f;
        float sy = cube.sizeY / 16f;
        float sz = cube.sizeZ / 16f;
        float inflateX = cube.inflateX / 16f;
        float inflateY = cube.inflateY / 16f;
        float inflateZ = cube.inflateZ / 16f;

        boolean flatX = sx == 0f;
        boolean flatY = sy == 0f;
        boolean flatZ = sz == 0f;

        final float eps = 5e-5f;
        if (flatX) {
            ox -= eps;
            sx = eps * 2f;
        }
        if (flatY) {
            oy -= eps;
            sy = eps * 2f;
        }
        if (flatZ) {
            oz -= eps;
            sz = eps * 2f;
        }

        TabulaCubeRenderer.renderCube(poseStack, consumer, ox, oy, oz, sx, sy, sz,
                inflateX, inflateY, inflateZ,
                cube.texU, cube.texV, cube.sizeX, cube.sizeY, cube.sizeZ,
                texW, texH,
                light, overlay, rgba[0], rgba[1], rgba[2], rgba[3], mirror, flatX, flatY, flatZ);
    }
}
