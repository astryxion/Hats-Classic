package com.astryxion.hats.common.network.trade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketNearbyPlayersList implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketNearbyPlayersList> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "nearby_players_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketNearbyPlayersList> STREAM_CODEC =
            StreamCodec.of(PacketNearbyPlayersList::encode, PacketNearbyPlayersList::decode);

    public record Entry(UUID uuid, String name) {}

    private final List<Entry> players;

    public PacketNearbyPlayersList(List<Entry> players) {
        this.players = List.copyOf(players);
    }

    public List<Entry> players() {
        return players;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketNearbyPlayersList msg) {
        buf.writeVarInt(msg.players.size());
        for (Entry entry : msg.players) {
            buf.writeUUID(entry.uuid());
            buf.writeUtf(entry.name());
        }
    }

    public static PacketNearbyPlayersList decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Entry> players = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            players.add(new Entry(buf.readUUID(), buf.readUtf()));
        }
        return new PacketNearbyPlayersList(players);
    }

    public static void handle(PacketNearbyPlayersList msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                com.astryxion.hats.client.trade.ClientTradePackets.onNearbyPlayers(msg);
            }
        });
    }
}
