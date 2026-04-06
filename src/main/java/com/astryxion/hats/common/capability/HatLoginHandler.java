package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hats.MODID)
public class HatLoginHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        HatPacketHandler.syncPlayerHatPartToTracking(player);
    }
}