package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.trade.TradeErrorType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketTradeError {

    private final TradeErrorType type;
    private final String details;

    public PacketTradeError(TradeErrorType type, String details) {
        this.type = type;
        this.details = details == null ? "" : details;
    }

    public TradeErrorType type() {
        return type;
    }

    public String details() {
        return details;
    }

    public static void encode(PacketTradeError msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.type);
        buf.writeUtf(msg.details);
    }

    public static PacketTradeError decode(FriendlyByteBuf buf) {
        return new PacketTradeError(buf.readEnum(TradeErrorType.class), buf.readUtf());
    }

    public static void handle(PacketTradeError msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.astryxion.hats.client.trade.ClientTradePackets.onError(msg)));
        ctx.get().setPacketHandled(true);
    }
}
