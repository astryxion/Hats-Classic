package com.astryxion.hats.client;

import com.astryxion.hats.Hats;
import com.astryxion.hats.client.gui.HatConfigScreen;
import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.gui.modern.ModernGuiCamera;
import com.astryxion.hats.client.keybinds.HatKeybinds;
import com.astryxion.hats.client.render.HatEntityRenderer;
import com.astryxion.hats.client.render.HatStandModel;
import com.astryxion.hats.client.render.HatStandRenderer;
import com.astryxion.hats.common.hat.HatColor;
import com.astryxion.hats.common.hatstand.HatStandRegistry;
import com.astryxion.hats.common.launcher.HatLauncherRegistry;
import com.astryxion.hats.common.registry.HatItemRegistry;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Hats.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HatsClient {

    public static void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(HatsClient::onRegisterKeyMappings);
        modEventBus.addListener((FMLClientSetupEvent e) -> NeoForge.EVENT_BUS.addListener(HatsClient::onClientTick));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModLoadingContext.get().registerExtensionPoint(
                    IConfigScreenFactory.class,
                    () -> (container, parent) -> new HatConfigScreen(parent)
            );
        });
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(HatKeybinds.OPEN_HATS);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? 0xFF000000 | HatColor.get(stack) : 0xFFFFFFFF,
                HatItemRegistry.getAllHats().toArray(net.minecraft.world.item.Item[]::new)
        );
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HatStandRegistry.HAT_STAND_ENTITY.get(), HatStandRenderer::new);
        event.registerEntityRenderer(HatLauncherRegistry.HAT_ENTITY.get(), HatEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HatStandRenderer.LAYER, HatStandModel::createLayer);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        ModernGuiCamera.tick();
        if (HatKeybinds.OPEN_HATS == null) {
            return;
        }
        while (HatKeybinds.OPEN_HATS.consumeClick()) {
            if (client.player == null) {
                return;
            }
            if (client.screen == null) {
                ModernGuiCamera.resetOrbit();
                HatsGui.open();
            }
        }
    }
}
