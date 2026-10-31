package com.astryxion.hats.common.network.trade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.astryxion.hats.common.trade.TradeErrorType;

import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.Supplier;

public class PacketTradeError implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketTradeError> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "trade_error"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTradeError> STREAM_CODEC =
            StreamCodec.of(PacketTradeError::encode, PacketTradeError::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final TradeErrorType type;
    private final String details;

    public PacketTradeError(TradeErrorType type, String details) {
        this.type = type;
        this.details = details == null ? "" : details;
    }

    public TradeErrorType errorType() {
        return type;
    }

    public String details() {
        return details;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketTradeError msg) {
        buf.writeEnum(msg.type);
        buf.writeUtf(msg.details);
    }

    public static PacketTradeError decode(RegistryFriendlyByteBuf buf) {
        return new PacketTradeError(buf.readEnum(TradeErrorType.class), buf.readUtf());
    }

    public static void handle(PacketTradeError msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.trade.ClientTradePackets",
                "onError",
                new Class<?>[]{PacketTradeError.class},
                new Object[]{msg}));
        
    }
}
