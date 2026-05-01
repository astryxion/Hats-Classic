package com.astryxion.hats.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Positions hats on Mo' Bends–animated entities without a compile dependency on Mo' Bends.
 * <p>
 * Mo' Bends replaces {@code AgeableListModel.renderToBuffer} with custom {@code BendsModelPart}
 * rendering and only copies rotation (not full translation) onto vanilla {@code ModelPart}s for
 * bipeds. Hat layers that call {@code head.translateAndRotate} therefore misalign.
 * <p>
 * During rendering Mo' Bends stores the active mutator in {@code MoBendsRenderContext}; it remains
 * set while render layers run. We apply {@code BendsModelPart.applyCharacterTransformPoseStack} on
 * the same part Mo' Bends uses for the head (or wolf / spider head). Squid used a thread-local
 * mutator in older Mo' Bends; 1.21.x NeoForge Mo' Bends no longer exposes it on
 * {@code MoBendsRenderContext}, so squid is not handled here.
 */
public final class MoBendsHatCompat {

    private static final String MOBENDS_MOD_ID = "mobends";
    private static final String RENDER_CONTEXT = "goblinbob.mobends.core.client.MoBendsRenderContext";

    private static Boolean mobendsPresent;
    private static boolean bipedReflectFailed;
    private static boolean coreReflectFailed;
    private static Method getCurrentBipedMutator;
    private static Method getCurrentWolfMutator;
    private static Method getCurrentSpiderMutator;
    private static Method shouldRenderCustom;
    private static Method getHead;
    private static Method applyCharacterTransformPoseStack;

    private MoBendsHatCompat() {}

    public static boolean isMoBendsLoaded() {
        if (mobendsPresent == null) {
            mobendsPresent = FabricLoader.getInstance().isModLoaded(MOBENDS_MOD_ID);
        }
        return mobendsPresent;
    }

    /**
     * Tries Mo' Bends animated geometry for the entity currently being rendered (biped, wolf, spider).
     *
     * @return true if a mutator applied a transform; false to use vanilla head path
     */
    public static boolean tryApplyAnimatedMobendsHead(PoseStack poseStack) {
        if (!isMoBendsLoaded()) {
            return false;
        }
        if (!ensureCoreReflected()) {
            return false;
        }
        if (tryApplyAnimatedBipedHead(poseStack)) {
            return true;
        }
        if (tryApplyWolfHead(poseStack)) {
            return true;
        }
        return tryApplySpiderHead(poseStack);
    }

    static boolean tryApplyAnimatedBipedHead(PoseStack poseStack) {
        if (bipedReflectFailed) {
            return false;
        }
        if (!ensureBipedReflected()) {
            return false;
        }
        try {
            Object mutator = getCurrentBipedMutator.invoke(null);
            if (mutator == null) {
                return false;
            }
            if (!(boolean) shouldRenderCustom.invoke(mutator)) {
                return false;
            }
            Object head = getHead.invoke(mutator);
            if (head == null) {
                return false;
            }
            applyCharacterTransformPoseStack.invoke(head, poseStack);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean tryApplyWolfHead(PoseStack poseStack) {
        try {
            if (getCurrentWolfMutator == null) {
                Class<?> ctx = Class.forName(RENDER_CONTEXT);
                getCurrentWolfMutator = ctx.getMethod("getCurrentWolfMutator");
            }
            Object mutator = getCurrentWolfMutator.invoke(null);
            if (mutator == null || !(boolean) shouldRenderCustom.invoke(mutator)) {
                return false;
            }
            Field f = mutator.getClass().getField("wolfHeadMain");
            Object head = f.get(mutator);
            if (head == null) {
                return false;
            }
            applyCharacterTransformPoseStack.invoke(head, poseStack);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean tryApplySpiderHead(PoseStack poseStack) {
        try {
            if (getCurrentSpiderMutator == null) {
                Class<?> ctx = Class.forName(RENDER_CONTEXT);
                getCurrentSpiderMutator = ctx.getMethod("getCurrentSpiderMutator");
            }
            Object mutator = getCurrentSpiderMutator.invoke(null);
            if (mutator == null || !(boolean) shouldRenderCustom.invoke(mutator)) {
                return false;
            }
            Field f = mutator.getClass().getField("spiderHead");
            Object head = f.get(mutator);
            if (head == null) {
                return false;
            }
            applyCharacterTransformPoseStack.invoke(head, poseStack);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean ensureCoreReflected() {
        if (coreReflectFailed) {
            return false;
        }
        if (shouldRenderCustom != null && applyCharacterTransformPoseStack != null) {
            return true;
        }
        try {
            shouldRenderCustom = Class.forName("goblinbob.mobends.core.mutators.Mutator")
                    .getMethod("shouldRenderCustom");
            Class<?> bendsPart = Class.forName("goblinbob.mobends.core.client.model.BendsModelPart");
            applyCharacterTransformPoseStack = bendsPart.getMethod("applyCharacterTransformPoseStack", PoseStack.class);
            return true;
        } catch (Throwable t) {
            coreReflectFailed = true;
            return false;
        }
    }

    private static boolean ensureBipedReflected() {
        if (bipedReflectFailed) {
            return false;
        }
        if (getCurrentBipedMutator != null && getHead != null) {
            return true;
        }
        if (!ensureCoreReflected()) {
            return false;
        }
        try {
            Class<?> ctx = Class.forName(RENDER_CONTEXT);
            getCurrentBipedMutator = ctx.getMethod("getCurrentBipedMutator");
            Class<?> mutatorClass = Class.forName("goblinbob.mobends.standard.mutators.BipedMutator");
            getHead = mutatorClass.getMethod("getHead");
            return true;
        } catch (Throwable t) {
            bipedReflectFailed = true;
            return false;
        }
    }
}
