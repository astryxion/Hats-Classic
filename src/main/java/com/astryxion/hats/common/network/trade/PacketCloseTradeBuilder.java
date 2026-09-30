package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.trade.TradeOfferStore;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCloseTradeBuilder {

    public static void encode(PacketCloseTradeBuilder msg, FriendlyByteBuf buf) {
    }

    public static PacketCloseTradeBuilder decode(FriendlyByteBuf buf) {
        return new PacketCloseTradeBuilder();
    }

    public static void handle(PacketCloseTradeBuilder msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                TradeOfferStore.get().closeSession(sender.getUUID());
            }
        });
        context.setPacketHandled(true);
    }
}
