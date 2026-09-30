package com.astryxion.hats.client;

import com.astryxion.hats.Hats;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hats.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .renderers
                .values()
                .forEach(ClientSetup::addIfMissing);
    }
}
