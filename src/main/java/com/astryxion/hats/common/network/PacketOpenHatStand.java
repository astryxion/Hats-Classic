package com.astryxion.hats.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketOpenHatStand(int entityId) {

    public static void encode(PacketOpenHatStand message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
    }

    public static PacketOpenHatStand decode(FriendlyByteBuf buffer) {
        return new PacketOpenHatStand(buffer.readVarInt());
    }

    public static void handle(PacketOpenHatStand message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            try {
                Class.forName("com.astryxion.hats.client.gui.HatStandGui")
                        .getMethod("open", int.class)
                        .invoke(null, message.entityId);
            } catch (ReflectiveOperationException ignored) {
            }
        });
        context.setPacketHandled(true);
    }
}
