package com.astryxion.hats.client;

import com.astryxion.hats.client.render.HatRenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {

        Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .renderers
                .values()
                .forEach(renderer -> {

                    if (renderer instanceof LivingEntityRenderer livingRenderer) {

                        livingRenderer.addLayer(
                                new HatRenderLayer(livingRenderer)
                        );
                    }
                });
    }
}
