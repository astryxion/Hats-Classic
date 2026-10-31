package com.astryxion.hats.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.Supplier;

public record PacketOpenHatStand(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketOpenHatStand> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "open_hat_stand"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenHatStand> STREAM_CODEC =
            StreamCodec.of(PacketOpenHatStand::encode, PacketOpenHatStand::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void encode(RegistryFriendlyByteBuf buffer, PacketOpenHatStand message) {
        buffer.writeVarInt(message.entityId);
    }

    public static PacketOpenHatStand decode(RegistryFriendlyByteBuf buffer) {
        return new PacketOpenHatStand(buffer.readVarInt());
    }

    public static void handle(PacketOpenHatStand message, IPayloadContext supplier) {
        IPayloadContext context = supplier;
        context.enqueueWork(() -> {
            try {
                Class.forName("com.astryxion.hats.client.gui.HatStandGui")
                        .getMethod("open", int.class)
                        .invoke(null, message.entityId);
            } catch (ReflectiveOperationException ignored) {
            }
        });
    }
}
