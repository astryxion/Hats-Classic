package com.astryxion.hats.client.tabula;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class TabulaHatRenderTypes extends RenderType {

    protected TabulaHatRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
            int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
            Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static final Function<ResourceLocation, RenderType> HAT_TRANSLUCENT =
            Util.memoize(texture -> {
                CompositeState state = CompositeState.builder()
                        .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader))
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false);
                return create("hats_tabula_translucent_no_cull",
                        DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                        1536, true, true, state);
            });

    private static final Function<ResourceLocation, RenderType> HAT_TRANSLUCENT_CULL =
            Util.memoize(texture -> {
                CompositeState state = CompositeState.builder()
                        .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentCullShader))
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false);
                return create("hats_tabula_translucent",
                        DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                        1536, true, true, state);
            });

    public static RenderType hatTranslucent(ResourceLocation texture) {
        return HAT_TRANSLUCENT.apply(texture);
    }

    public static RenderType hatTranslucentCull(ResourceLocation texture) {
        return HAT_TRANSLUCENT_CULL.apply(texture);
    }
}
