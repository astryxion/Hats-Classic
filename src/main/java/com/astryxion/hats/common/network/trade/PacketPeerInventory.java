package com.astryxion.hats.common.network.trade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PacketPeerInventory implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketPeerInventory> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "peer_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPeerInventory> STREAM_CODEC =
            StreamCodec.of(PacketPeerInventory::encode, PacketPeerInventory::decode);

    private final UUID owner;
    @Nullable
    private final Identifier equipped;
    private final List<Identifier> hats;

    public PacketPeerInventory(UUID owner, @Nullable Identifier equipped, List<Identifier> hats) {
        this.owner = owner;
        this.equipped = equipped;
        this.hats = List.copyOf(hats);
    }

    public UUID owner() {
        return owner;
    }

    @Nullable
    public Identifier equipped() {
        return equipped;
    }

    public List<Identifier> hats() {
        return hats;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketPeerInventory msg) {
        buf.writeUUID(msg.owner);
        buf.writeBoolean(msg.equipped != null);
        if (msg.equipped != null) {
            buf.writeIdentifier(msg.equipped);
        }
        TradePacketBuffers.writeIds(buf, msg.hats);
    }

    public static PacketPeerInventory decode(RegistryFriendlyByteBuf buf) {
        UUID owner = buf.readUUID();
        Identifier equipped = buf.readBoolean() ? buf.readIdentifier() : null;
        return new PacketPeerInventory(owner, equipped, TradePacketBuffers.readIds(buf));
    }

    public static void handle(PacketPeerInventory msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                com.astryxion.hats.client.trade.ClientTradePackets.onPeerInventory(msg);
            }
        });
    }
}
