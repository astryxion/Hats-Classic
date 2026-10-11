package com.astryxion.hats.client.render;

import com.astryxion.hats.client.tabula.TabulaHatPlacement;
import com.astryxion.hats.client.tabula.TabulaHatRenderer;
import com.astryxion.hats.common.tabula.TabulaHats;
import com.astryxion.hats.mixin.ModelPartAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Capture the head bone matrix during {@code translateAndRotate}, then draw the
 * hat only after {@code DragonModel.renderToBuffer} finishes — drawing items
 * mid-pass rebinds textures and explodes the dragon mesh.
 */
public final class EnderDragonHatRenderer {

    private static final float PLAYER_HEAD_SIZE = 8.0F;
    private static final float ITEM_HEAD_SCALE = 0.625F;
    private static final float DRAGON_HAT_SCALE = 4.0F;

    private static final ThreadLocal<SubmitNodeCollector> COLLECTOR = new ThreadLocal<>();
    private static final ThreadLocal<Integer> LIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> PASS = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<EnderDragon> DRAGON = new ThreadLocal<>();
    private static final ThreadLocal<ModelPart> HEAD = new ThreadLocal<>();
    private static final ThreadLocal<Matrix4f> HEAD_POSE = new ThreadLocal<>();
    private static final ThreadLocal<Matrix3f> HEAD_NORMAL = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> DRAWN = ThreadLocal.withInitial(() -> false);

    private EnderDragonHatRenderer() {}

    public static void beginFrame(SubmitNodeCollector collector, int packedLight) {
        COLLECTOR.set(collector);
        LIGHT.set(packedLight);
        PASS.set(0);
        DRAWN.set(false);
        HEAD_POSE.remove();
        HEAD_NORMAL.remove();
    }

    public static void endFrame() {
        COLLECTOR.remove();
        LIGHT.remove();
        DRAGON.remove();
        HEAD.remove();
        HEAD_POSE.remove();
        HEAD_NORMAL.remove();
        DRAWN.remove();
        PASS.remove();
    }

    public static EnderDragon dragon() {
        return DRAGON.get();
    }

    public static void bindDragon(EnderDragon dragon) {
        DRAGON.set(dragon);
    }

    public static void finishPass() {
        int pass = PASS.get() + 1;
        PASS.set(pass);
        if (pass >= 2) {
            endFrame();
        }
    }

    public static void bindHead(EnderDragon dragon, ModelPart head) {
        DRAGON.set(dragon);
        HEAD.set(head);
    }

    public static void unbindHead() {
        DRAGON.remove();
        HEAD.remove();
    }

    /** Snapshot only — never draw here (still inside the dragon mesh pass). */
    public static void onHeadTranslated(ModelPart part, PoseStack poseStack) {
        if (part != HEAD.get()) {
            return;
        }
        if (HEAD_POSE.get() != null) {
            return;
        }
        HEAD_POSE.set(new Matrix4f(poseStack.last().pose()));
        HEAD_NORMAL.set(new Matrix3f(poseStack.last().normal()));
    }

    /** Safe to draw: model geometry for this pass is finished. */
    public static void flushAfterModel(PoseStack poseStack) {
        if (Boolean.TRUE.equals(DRAWN.get())) {
            return;
        }
        Matrix4f headPose = HEAD_POSE.get();
        Matrix3f headNormal = HEAD_NORMAL.get();
        SubmitNodeCollector collector = COLLECTOR.get();
        Integer packedLight = LIGHT.get();
        EnderDragon dragon = DRAGON.get();
        ModelPart head = HEAD.get();
        if (headPose == null || headNormal == null || collector == null
                || packedLight == null || dragon == null || head == null) {
            return;
        }

        ItemStack hat = ClientHatPreview.orWorn(dragon);
        if (hat.isEmpty()) {
            return;
        }
        DRAWN.set(true);

        poseStack.pushPose();
        poseStack.last().pose().set(headPose);
        poseStack.last().normal().set(headNormal);

        float[] cube = upperSkullCube(head);
        if (cube != null) {
            float w = cube[3] - cube[0];
            float h = cube[4] - cube[1];
            float d = cube[5] - cube[2];
            float top = cube[1];
            float hatHalf = Math.min(w, h) / 2.0F;
            float originY = (top + hatHalf * 0.35F) / 16.0F;
            float originZ = (cube[2] + Math.min(PLAYER_HEAD_SIZE, d) * 0.5F) / 16.0F;
            poseStack.translate(
                    (cube[0] + cube[3]) / 2.0F / 16.0F,
                    originY,
                    originZ
            );
        }

        poseStack.scale(DRAGON_HAT_SCALE, DRAGON_HAT_SCALE, DRAGON_HAT_SCALE);

        if (TabulaHats.isTabula(hat)) {
            float geometryScale = 1.0F;
            if (cube != null) {
                geometryScale = Mth.clamp((cube[3] - cube[0]) / PLAYER_HEAD_SIZE, 0.2F, 1.0F);
            }
            TabulaHatPlacement.applyZFight(poseStack);
            TabulaHatRenderer.render(
                    poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, hat, geometryScale);
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(ITEM_HEAD_SCALE, -ITEM_HEAD_SCALE, -ITEM_HEAD_SCALE);
            HatRenderLayer.submitHatItem(poseStack, collector, dragon, hat, packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private static float[] upperSkullCube(ModelPart head) {
        try {
            List<ModelPart.Cube> cubes = ((ModelPartAccessor) (Object) head).hats$cubes();
            if (cubes == null || cubes.isEmpty()) {
                return null;
            }
            ModelPart.Cube best = null;
            float bestScore = Float.NEGATIVE_INFINITY;
            for (ModelPart.Cube cube : cubes) {
                float w = cube.maxX - cube.minX;
                float h = cube.maxY - cube.minY;
                float d = cube.maxZ - cube.minZ;
                if (h < d * 0.75F) {
                    continue;
                }
                float score = (h / Math.max(1.0F, d)) * 10.0F + w - cube.minY;
                if (score > bestScore) {
                    bestScore = score;
                    best = cube;
                }
            }
            if (best == null) {
                float bestVol = 0.0F;
                for (ModelPart.Cube cube : cubes) {
                    float vol = (cube.maxX - cube.minX)
                            * (cube.maxY - cube.minY)
                            * (cube.maxZ - cube.minZ);
                    if (vol > bestVol) {
                        bestVol = vol;
                        best = cube;
                    }
                }
            }
            if (best == null) {
                return null;
            }
            return new float[]{best.minX, best.minY, best.minZ, best.maxX, best.maxY, best.maxZ};
        } catch (Throwable t) {
            return null;
        }
    }
}
