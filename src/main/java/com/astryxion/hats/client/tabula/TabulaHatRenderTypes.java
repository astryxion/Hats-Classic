package com.astryxion.hats.client.tabula;

import java.util.function.Function;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class TabulaHatRenderTypes {

    private TabulaHatRenderTypes() {}

    public static RenderType hatTranslucent(Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    private static final Function<Identifier, RenderType> HAT_TRANSLUCENT_CULL = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                .createRenderSetup();
        return RenderType.create("hats_tabula_translucent", state);
    });

    public static RenderType hatTranslucentCull(Identifier texture) {
        return HAT_TRANSLUCENT_CULL.apply(texture);
    }
}
