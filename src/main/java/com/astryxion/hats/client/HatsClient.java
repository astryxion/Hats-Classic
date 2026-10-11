package com.astryxion.hats.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;
import com.astryxion.hats.client.gui.HatConfigScreen;
import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.gui.modern.ModernGuiCamera;
import com.astryxion.hats.client.keybinds.HatKeybinds;
import com.astryxion.hats.client.render.ClientHatPreview;
import com.astryxion.hats.client.render.HatEntityRenderer;
import com.astryxion.hats.client.render.HatRenderLayer;
import com.astryxion.hats.client.render.HatStandModel;
import com.astryxion.hats.client.render.HatStandRenderer;
import com.astryxion.hats.client.render.HatLauncherClientExtensions;
import com.astryxion.hats.client.tabula.TabulaHatClientExtensions;
import com.astryxion.hats.common.hat.HatColor;
import com.astryxion.hats.common.network.PacketSyncHatPart;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.hatstand.HatStandRegistry;
import com.astryxion.hats.common.launcher.HatLauncherRegistry;

import com.google.common.reflect.TypeToken;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT)
public class HatsClient {

    public static void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(HatsClient::onRegisterKeyMappings);
        modEventBus.addListener((FMLClientSetupEvent e) -> NeoForge.EVENT_BUS.addListener(HatsClient::onClientTick));
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(HatLauncherClientExtensions.INSTANCE, HatLauncherRegistry.HAT_LAUNCHER.get());
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof HatItem hatItem && hatItem.isTabula()) {
                event.registerItem(TabulaHatClientExtensions.INSTANCE, hatItem);
            }
        }
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

    /**
     * 26.1 clears render-state data after extract, then runs these modifiers.
     * The hat has to be copied here or the layer submits an empty stack.
     */
    @SubscribeEvent
    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {},
                (LivingEntity entity, LivingEntityRenderState state) -> {
                    state.setRenderData(HatRenderLayer.HAT_ENTITY, entity);
                    ItemStack hat = ClientHatPreview.orWorn(entity);
                    state.setRenderData(HatRenderLayer.HAT_STACK, hat.isEmpty() ? ItemStack.EMPTY : hat.copy());
                }
        );
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(Hats.MODID, "hat_color"), HatItemTint.CODEC);
    }

    public static final class HatItemTint implements ItemTintSource {
        public static final com.mojang.serialization.MapCodec<HatItemTint> CODEC =
                com.mojang.serialization.MapCodec.unit(new HatItemTint());

        @Override
        public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
            return 0xFF000000 | HatColor.get(stack);
        }

        @Override
        public com.mojang.serialization.MapCodec<? extends ItemTintSource> type() {
            return CODEC;
        }
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
        PacketSyncHatPart.flushPending();
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
