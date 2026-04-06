package com.astryxion.hats.client;

import com.astryxion.hats.Hats;
import com.astryxion.hats.client.render.HatRenderLayer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void addLayers(EntityRenderersEvent.AddLayers event) {

        // YOUR forge gives Set<String> skins
        for (String skin : event.getSkins()) {

            EntityRenderer<?> renderer = event.getSkin(skin);

            if (renderer instanceof LivingEntityRenderer livingRenderer) {

                livingRenderer.addLayer(
                        new HatRenderLayer(livingRenderer)
                );
            }
        }
    }
}
