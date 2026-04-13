package com.astryxion.hats.client.render;

import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.client.render.helper.HatRendererHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;

@SuppressWarnings({"rawtypes", "unchecked"})
public class HatRenderLayer extends RenderLayer<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> {

    public HatRenderLayer(LivingEntityRenderer<?, LivingEntityRenderState, ?> parent) {
        super((LivingEntityRenderer<?, LivingEntityRenderState, EntityModel<LivingEntityRenderState>>) (Object) parent);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int lightness,
            LivingEntityRenderState renderState,
            float netHeadYaw,
            float headPitch
    ) {
        LivingEntity entity = renderState.getRenderData(HatRenderStateKeys.LIVING_ENTITY);
        if (entity == null) {
            return;
        }

        ItemStack hat = HatManager.getHat(entity);
        if (hat.isEmpty()) {
            return;
        }

        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

        poseStack.pushPose();

        EntityModel<LivingEntityRenderState> model = getParentModel();

        boolean applied = false;

        if (model instanceof HumanoidModel<?> humanoid) {
            humanoid.head.translateAndRotate(poseStack);
            applied = true;
        }

        if (!applied) {
            ModelPart head = findHeadPart(model);
            if (head != null) {
                head.translateAndRotate(poseStack);
            }
        }

        HatRendererHelper.applyTransforms(entity, poseStack, partialTicks);

        Level level = entity.level();
        ItemStackRenderState stackRenderState = new ItemStackRenderState();
        Minecraft.getInstance()
                .getItemModelResolver()
                .updateForTopItem(stackRenderState, hat, ItemDisplayContext.HEAD, level, entity, entity.getId());
        stackRenderState.submit(poseStack, nodeCollector, lightness, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    private ModelPart findHeadPart(Object model) {
        if (model instanceof HumanoidModel<?> humanoid) {
            return humanoid.head;
        }

        Class<?> search = model.getClass();
        while (search != null && search != Object.class) {
            for (Field field : search.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value instanceof ModelPart part) {
                        String name = field.getName().toLowerCase();
                        if (name.contains("head") || name.startsWith("f_")) {
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
