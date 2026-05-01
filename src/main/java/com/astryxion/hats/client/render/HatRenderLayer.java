package com.astryxion.hats.client.render;

import com.astryxion.hats.client.compat.MoBendsHatCompat;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.client.render.helper.HatRendererHelper;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class HatRenderLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    /**
     * Animal / quadruped models do not implement {@link HeadedModel}; they expose head bones via
     * a no-arg method returning {@code Iterable<ModelPart>} (mojmap historically {@code headParts()} /
     * yarn {@code getHeadParts()}). At runtime those method and field names are obfuscated, so we
     * resolve the accessor once per concrete model class by picking the iterable with the fewest
     * {@link ModelPart} entries (head group is smaller than body+legs for sheep, pigs, cows, etc.).
     */
    private Method hats$resolvedHeadPartsAccessor;
    private Class<?> hats$resolvedHeadModelClass;

    public HatRenderLayer(LivingEntityRenderer<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {

        ItemStack hat = HatManager.getHat(entity);

        if (hat.isEmpty())
            return;

        poseStack.pushPose();

        EntityModel<T> model = getParentModel();

        boolean applied = false;

        // Mo' Bends: BendsModelPart rigs; vanilla ModelPart sync is incomplete for layers (see MoBendsHatCompat).
        if (MoBendsHatCompat.isMoBendsLoaded() && MoBendsHatCompat.tryApplyAnimatedMobendsHead(poseStack)) {
            applied = true;
        }

        // ============================
        // MODERN DEV PATH
        // ============================

        if (!applied && model instanceof HeadedModel headed) {
            headed.getHead().translateAndRotate(poseStack);
            applied = true;
        }

        // ============================
        // PRODUCTION SAFE FALLBACK
        // ============================

        if (!applied) {

            ModelPart head = resolveNonHumanoidHeadPart(model);

            if (head != null) {
                head.translateAndRotate(poseStack);
            }
        }

        // ============================
        // Your helper offsets
        // ============================

        HatRendererHelper.applyTransforms(
                entity,
                poseStack,
                partialTicks
        );

        // ============================
        // Render hat item
        // ============================

        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity,
                hat,
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                buffer,
                entity.level(),
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }

    // ============================
    // Heuristic animated head finder
    // (works dev + production)
    // ============================

    private ModelPart resolveNonHumanoidHeadPart(EntityModel<T> model) {
        ModelPart viaIterable = tryHeadPartFromSmallestIterableMethod(model);
        if (viaIterable != null) {
            return viaIterable;
        }
        return findHeadPart(model);
    }

    private ModelPart tryHeadPartFromSmallestIterableMethod(EntityModel<T> model) {
        Class<?> mc = model.getClass();
        if (hats$resolvedHeadPartsAccessor == null || hats$resolvedHeadModelClass != mc) {
            hats$resolvedHeadPartsAccessor = resolveSmallestModelPartIterableMethod(model);
            hats$resolvedHeadModelClass = mc;
        }
        Method m = hats$resolvedHeadPartsAccessor;
        if (m == null) {
            return null;
        }
        try {
            m.setAccessible(true);
            Object inv = m.invoke(model);
            if (!(inv instanceof Iterable<?> it)) {
                return null;
            }
            for (Object o : it) {
                if (o instanceof ModelPart part) {
                    return part;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    private static Method resolveSmallestModelPartIterableMethod(Object modelInst) {
        Method best = null;
        int bestCount = Integer.MAX_VALUE;
        for (Class<?> c = modelInst.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getParameterCount() != 0) {
                    continue;
                }
                if (m.isBridge() || Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                if (!Iterable.class.isAssignableFrom(m.getReturnType())) {
                    continue;
                }
                m.setAccessible(true);
                try {
                    Object inv = m.invoke(modelInst);
                    if (!(inv instanceof Iterable<?> it)) {
                        continue;
                    }
                    int n = 0;
                    for (Object o : it) {
                        if (o instanceof ModelPart) {
                            n++;
                        }
                    }
                    if (n > 0 && n < bestCount) {
                        bestCount = n;
                        best = m;
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
        return best;
    }

    private ModelPart findHeadPart(Object model) {

        // Try HeadedModel again (safety)
        if (model instanceof HeadedModel headed) {
            return headed.getHead();
        }

        Class<?> search = model.getClass();

        while (search != null && search != Object.class) {

            for (Field field : search.getDeclaredFields()) {

                try {
                    field.setAccessible(true);
                    Object value = field.get(model);

                    if (value instanceof ModelPart part) {
                        ModelPart named = findNamedHeadPart(part, new HashSet<>());
                        if (named != null) {
                            return named;
                        }

                        String name = field.getName().toLowerCase();
                        if (name.contains("head")) {
                            return part;
                        }
                    }

                } catch (Exception ignored) {}
            }

            search = search.getSuperclass();
        }

        return null;
    }

    private ModelPart findNamedHeadPart(ModelPart part, Set<ModelPart> visited) {
        if (!visited.add(part)) {
            return null;
        }

        if (part.hasChild("head")) {
            return part.getChild("head");
        }
        if (part.hasChild("real_head")) {
            return part.getChild("real_head");
        }
        if (part.hasChild("hat")) {
            return part.getChild("hat");
        }
        if (part.hasChild("nose")) {
            return part.getChild("nose");
        }

        for (ModelPart child : part.getAllParts().toList()) {
            if (child == part) {
                continue;
            }
            ModelPart nested = findNamedHeadPart(child, visited);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }
}
