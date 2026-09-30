package com.astryxion.hats.common.network.trade;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketTradePartnerLeft {

    private final UUID targetUuid;

    public PacketTradePartnerLeft(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    public static void encode(PacketTradePartnerLeft msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.targetUuid);
    }

    public static PacketTradePartnerLeft decode(FriendlyByteBuf buf) {
        return new PacketTradePartnerLeft(buf.readUUID());
    }

    public static void handle(PacketTradePartnerLeft msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.astryxion.hats.client.trade.ClientTradePackets.onPartnerLeft(msg)));
        ctx.get().setPacketHandled(true);
    }
}
