package com.astryxion.hats.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;
import com.astryxion.hats.client.compat.FreshAnimationsHatCompat;
import com.astryxion.hats.client.compat.SkinLayers3dHatCompat;
import com.astryxion.hats.client.render.HatRenderLayer;
import com.astryxion.hats.client.render.SkipModHatHeadLayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.PlayerModelType;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.List;

@EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(
                Identifier.fromNamespaceAndPath(Hats.MODID, "fresh_animations"),
                (ResourceManagerReloadListener) FreshAnimationsHatCompat::onResourceManagerReload
        );
    }

    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            addIfMissing(event.getPlayerRenderer(skin));
        }
        Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .renderers
                .values()
                .forEach(ClientSetup::addIfMissing);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void addIfMissing(EntityRenderer<?, ?> renderer) {
        if (!(renderer instanceof LivingEntityRenderer livingRenderer)) {
            return;
        }
        if (!alreadyHasHatLayer(livingRenderer)) {
            livingRenderer.addLayer(new HatRenderLayer(livingRenderer));
        }
        wrapCustomHeadLayer(livingRenderer);
        orderHatLayerAfterSkinLayersHead(livingRenderer);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void wrapCustomHeadLayer(LivingEntityRenderer<?, ?, ?> renderer) {
        List<?> layers = layersOf(renderer);
        for (int i = 0; i < layers.size(); i++) {
            Object layer = layers.get(i);
            if (layer instanceof CustomHeadLayer && !(layer instanceof SkipModHatHeadLayer)) {
                ((List) layers).set(i, new SkipModHatHeadLayer(renderer, (RenderLayer) layer));
            }
        }
    }

    private static boolean alreadyHasHatLayer(LivingEntityRenderer<?, ?, ?> renderer) {
        for (Object layer : layersOf(renderer)) {
            if (layer instanceof HatRenderLayer) {
                return true;
            }
        }
        return false;
    }

    private static void orderHatLayerAfterSkinLayersHead(LivingEntityRenderer<?, ?, ?> renderer) {
        List<?> layers = layersOf(renderer);
        for (Object layer : layers) {
            if (layer instanceof HatRenderLayer) {
                SkinLayers3dHatCompat.moveHatLayerAfterSkinHead(layers, layer);
                return;
            }
        }
    }

    private static List<?> layersOf(LivingEntityRenderer<?, ?, ?> renderer) {
        for (String name : new String[]{"layers", "f_115291_"}) {
            try {
                Field field = LivingEntityRenderer.class.getDeclaredField(name);
                field.setAccessible(true);
                Object value = field.get(renderer);
                if (value instanceof List<?> list) {
                    return list;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        for (Field field : LivingEntityRenderer.class.getDeclaredFields()) {
            if (!List.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(renderer);
                if (value instanceof List<?> list) {
                    return list;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return List.of();
    }
}
