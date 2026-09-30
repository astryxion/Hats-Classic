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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Hats.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HatsClient {

    public static void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(HatsClient::onRegisterKeyMappings);
        modEventBus.addListener((FMLClientSetupEvent e) -> MinecraftForge.EVENT_BUS.addListener(HatsClient::onClientTick));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, parent) -> new HatConfigScreen(parent)
                    )
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

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
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
