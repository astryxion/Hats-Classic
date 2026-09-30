package com.astryxion.hats.common.network.trade;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketNearbyPlayersList {

    public record Entry(UUID uuid, String name) {}

    private final List<Entry> players;

    public PacketNearbyPlayersList(List<Entry> players) {
        this.players = List.copyOf(players);
    }

    public List<Entry> players() {
        return players;
    }

    public static void encode(PacketNearbyPlayersList msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.players.size());
        for (Entry entry : msg.players) {
            buf.writeUUID(entry.uuid());
            buf.writeUtf(entry.name());
        }
    }

    public static PacketNearbyPlayersList decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Entry> players = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            players.add(new Entry(buf.readUUID(), buf.readUtf()));
        }
        return new PacketNearbyPlayersList(players);
    }

    public static void handle(PacketNearbyPlayersList msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.astryxion.hats.client.trade.ClientTradePackets.onNearbyPlayers(msg)));
        ctx.get().setPacketHandled(true);
    }
}
