package com.astryxion.hats.client;

import com.astryxion.hats.client.render.HatRenderLayer;
import com.astryxion.hats.client.render.HatRenderStateKeys;
import com.google.common.reflect.TypeToken;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

public final class HatClientRenderSetup {
    private HatClientRenderSetup() {}

    public static void register(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener(HatClientRenderSetup::onRegisterRenderStateModifiers);
        modBus.addListener(HatClientRenderSetup::onAddLayers);
    }

    private static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {},
                (entity, state) -> state.setRenderData(HatRenderStateKeys.LIVING_ENTITY, entity));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            var renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer ler) {
                ler.addLayer(new HatRenderLayer(ler));
            }
        }
        for (var skin : event.getSkins()) {
            AvatarRenderer<?> playerRenderer = event.getPlayerRenderer(skin);
            if (playerRenderer != null) {
                ((LivingEntityRenderer) (Object) playerRenderer).addLayer(new HatRenderLayer((LivingEntityRenderer) (Object) playerRenderer));
            }
            var mannequin = event.getMannequinRenderer(skin);
            if (mannequin != null) {
                ((LivingEntityRenderer) (Object) mannequin).addLayer(new HatRenderLayer((LivingEntityRenderer) (Object) mannequin));
            }
        }
    }
}
