package com.astryxion.hats.common.capability;

import net.neoforged.fml.common.EventBusSubscriber;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = Hats.MODID)
public class HatLoginHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        HatPacketHandler.syncPlayerHatPartToTracking(player);
    }
}