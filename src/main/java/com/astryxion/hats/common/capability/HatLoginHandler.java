package com.astryxion.hats.common.capability;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatPart;
import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketSyncHatPart;

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

        player.getCapability(HatPartCapability.HAT_PART).ifPresent(part -> {

            // 🔧 FIXED: Added player.getId() to match the new PacketSyncHatPart constructor
            HatPacketHandler.sendToPlayer(
                    player,
                    new PacketSyncHatPart(player.getId(), part.serializeNBT())
            );
        });
    }
}