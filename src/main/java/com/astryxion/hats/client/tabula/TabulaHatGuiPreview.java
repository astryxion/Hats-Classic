package com.astryxion.hats.client.tabula;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.tabula.TabulaHats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

/**
 * Draws a Tabula hat in a GUI slot the same way Hats Renewed draws toast hats:
 * no Y-flip. {@code renderFakeItem} cannot show these because they are entity
 * models, not 2D item icons.
 */
@EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT)
public final class TabulaHatGuiPreview {

    private TabulaHatGuiPreview() {}

    @SubscribeEvent
    public static void registerRenderer(RegisterPictureInPictureRenderersEvent event) {
        event.register(PreviewState.class, PreviewRenderer::new);
    }

    public static void render(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int size) {
        if (stack == null || stack.isEmpty() || !TabulaHats.isTabula(stack)) {
            return;
        }
        graphics.submitPictureInPictureRenderState(new PreviewState(
                x,
                y,
                x + size,
                y + size,
                size * 0.7F,
                stack.copy(),
                graphics.peekScissorStack(),
                new Matrix3x2f(graphics.pose())
        ));
    }

    public record PreviewState(
            int x0,
            int y0,
            int x1,
            int y1,
            float scale,
            ItemStack stack,
            @Nullable ScreenRectangle scissorArea,
            Matrix3x2f pose
    ) implements PictureInPictureRenderState {
        @Override
        public @Nullable ScreenRectangle bounds() {
            return PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea);
        }
    }

    public static final class PreviewRenderer extends PictureInPictureRenderer<PreviewState> {
        public PreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
            super(bufferSource);
        }

        @Override
        public Class<PreviewState> getRenderStateClass() {
            return PreviewState.class;
        }

        @Override
        protected void renderToTexture(PreviewState state, PoseStack poseStack) {
            poseStack.scale(1.0F, 1.0F, -1.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(205.0F));
            TabulaHatRenderer.render(poseStack, this.bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, state.stack(), 1.0F);
        }

        @Override
        protected float getTranslateY(int height, int guiScale) {
            return height / 2.0F;
        }

        @Override
        protected String getTextureLabel() {
            return "hats_tabula_preview";
        }
    }
}
