package com.astryxion.hats.client.render;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class HatRenderLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    /**
     * Per concrete model class: method that returns the smallest non-empty {@link Iterable} of
     * {@link ModelPart} (vanilla head vs body grouping), or empty if probing failed.
     */
    private final ConcurrentHashMap<Class<?>, Optional<Method>> headIterableMethodByModelClass =
            new ConcurrentHashMap<>();

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

        if (model instanceof HeadedModel headed) {
            headed.getHead().translateAndRotate(poseStack);
            applied = true;
        }

        if (!applied) {

            ModelPart head = findHeadPart(model);

            if (head != null) {
                head.translateAndRotate(poseStack);
            }
        }

        HatRendererHelper.applyTransforms(
                entity,
                poseStack,
                partialTicks
        );

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

    private ModelPart findHeadPart(Object model) {

        if (model instanceof HeadedModel headed) {
            return headed.getHead();
        }

        Optional<Method> cached = headIterableMethodByModelClass.computeIfAbsent(
                model.getClass(),
                k -> Optional.ofNullable(probeSmallestIterableHeadMethod(model))
        );
        ModelPart fromIterable = cached.map(m -> invokeFirstModelPart(model, m)).orElse(null);
        if (fromIterable != null) {
            return fromIterable;
        }

        return findHeadPartFieldFallback(model);
    }

    private static Method probeSmallestIterableHeadMethod(Object model) {
        List<Method> candidates = new ArrayList<>();
        Class<?> c = model.getClass();
        while (c != null && c != Object.class) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getParameterCount() != 0
                        || Modifier.isStatic(m.getModifiers())
                        || m.isBridge()
                        || !Iterable.class.isAssignableFrom(m.getReturnType())) {
                    continue;
                }
                m.setAccessible(true);
                candidates.add(m);
            }
            c = c.getSuperclass();
        }

        Method best = null;
        int bestCount = Integer.MAX_VALUE;
        for (Method m : candidates) {
            int count = countModelPartsInIterable(model, m);
            if (count > 0 && count < bestCount) {
                bestCount = count;
                best = m;
            }
        }
        return best;
    }

    private static int countModelPartsInIterable(Object model, Method m) {
        try {
            Object invoked = m.invoke(model);
            if (!(invoked instanceof Iterable<?> iterable)) {
                return 0;
            }
            int n = 0;
            for (Object o : iterable) {
                if (o instanceof ModelPart) {
                    n++;
                }
            }
            return n;
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            return 0;
        }
    }

    private static ModelPart invokeFirstModelPart(Object model, Method m) {
        try {
            Object invoked = m.invoke(model);
            if (!(invoked instanceof Iterable<?> iterable)) {
                return null;
            }
            for (Object o : iterable) {
                if (o instanceof ModelPart part) {
                    return part;
                }
            }
        } catch (ReflectiveOperationException ignored) {}
        return null;
    }

    private static ModelPart findHeadPartFieldFallback(Object model) {
        Class<?> search = model.getClass();
        while (search != null && search != Object.class) {
            for (Field field : search.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value instanceof ModelPart part) {
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
}
