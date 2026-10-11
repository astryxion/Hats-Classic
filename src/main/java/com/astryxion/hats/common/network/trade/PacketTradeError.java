package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.trade.TradeErrorType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * {@link #type()} already returns {@link TradeErrorType}, so this class cannot also
 * implement {@link CustomPacketPayload#type()}. {@link Payload} is the registered
 * payload and writes the same bytes.
 */
public class PacketTradeError {

    public static final CustomPacketPayload.Type<Payload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "trade_error"));

    public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC =
            StreamCodec.of(Payload::encode, Payload::decode);

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

    public static void encode(RegistryFriendlyByteBuf buf, PacketTradeError msg) {
        buf.writeEnum(msg.type);
        buf.writeUtf(msg.details);
    }

    public static PacketTradeError decode(RegistryFriendlyByteBuf buf) {
        return new PacketTradeError(buf.readEnum(TradeErrorType.class), buf.readUtf());
    }

    public static void handle(Payload msg, IPayloadContext ctx) {
        PacketTradeError packet = msg.packet();
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                com.astryxion.hats.client.trade.ClientTradePackets.onError(packet);
            }
        });
    }

    public static final class Payload implements CustomPacketPayload {

        private final PacketTradeError packet;

        public Payload(PacketTradeError packet) {
            this.packet = packet;
        }

        public PacketTradeError packet() {
            return packet;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void encode(RegistryFriendlyByteBuf buf, Payload msg) {
            PacketTradeError.encode(buf, msg.packet);
        }

        public static Payload decode(RegistryFriendlyByteBuf buf) {
            return new Payload(PacketTradeError.decode(buf));
        }
    }
}
