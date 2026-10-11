package com.astryxion.hats.client.tabula;

import com.astryxion.hats.common.tabula.TabulaHats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class TabulaHatItemRenderer implements ItemModel, SpecialModelRenderer<TabulaHatItemRenderer.RenderArg> {

    private static final ItemTransforms TRANSFORMS = loadDisplay();
    private static final Vector3fc[] EXTENTS = new Vector3fc[]{
            new Vector3f(-1.0F, -1.0F, -1.0F),
            new Vector3f(1.0F, 1.0F, 1.0F)
    };

    private static TabulaHatItemRenderer instance;

    public static TabulaHatItemRenderer getInstance() {
        if (instance == null) {
            instance = new TabulaHatItemRenderer();
        }
        return instance;
    }

    private TabulaHatItemRenderer() {}

    @Override
    public void update(
            ItemStackRenderState output,
            ItemStack item,
            ItemModelResolver resolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed
    ) {
        output.appendModelIdentityElement(this);
        output.setOversizedInGui(true);
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        layer.setItemTransform(TRANSFORMS.getTransform(displayContext));
        layer.setUsesBlockLight(false);
        layer.setExtents(() -> EXTENTS);
        layer.setupSpecialModel(this, new RenderArg(item, displayContext));
    }

    @Override
    public void submit(
            @Nullable RenderArg argument,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay,
            boolean hasFoil,
            int outlineColor
    ) {
        if (argument == null || !TabulaHats.isTabula(argument.stack())) {
            return;
        }
        int resolvedOverlay = overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.translate(0.0F, -8.0F / 16.0F, 0.0F);
        TabulaHatPlacement.applyZFight(poseStack);
        TabulaHatRenderer.render(poseStack, collector, light, resolvedOverlay, argument.stack(), 1.0F);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(EXTENTS[0]);
        output.accept(EXTENTS[1]);
    }

    @Override
    public @Nullable RenderArg extractArgument(ItemStack stack) {
        return new RenderArg(stack, ItemDisplayContext.NONE);
    }

    public record RenderArg(ItemStack stack, ItemDisplayContext context) {}

    private static ItemTransforms loadDisplay() {
        try (InputStream in = TabulaHatItemRenderer.class.getResourceAsStream("/assets/hats/models/item/tabula_hat.json")) {
            if (in == null) {
                return ItemTransforms.NO_TRANSFORMS;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!root.has("display")) {
                return ItemTransforms.NO_TRANSFORMS;
            }
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
                    .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
                    .create();
            return gson.fromJson(root.get("display"), ItemTransforms.class);
        } catch (Exception ignored) {
            return ItemTransforms.NO_TRANSFORMS;
        }
    }
}
