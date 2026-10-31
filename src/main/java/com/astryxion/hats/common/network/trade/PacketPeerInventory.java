package com.astryxion.hats.common.network.trade;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.astryxion.hats.common.network.HatPacketHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketPeerInventory implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketPeerInventory> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "peer_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPeerInventory> STREAM_CODEC =
            StreamCodec.of(PacketPeerInventory::encode, PacketPeerInventory::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final UUID owner;
    @Nullable
    private final ResourceLocation equipped;
    private final List<ResourceLocation> hats;

    public PacketPeerInventory(UUID owner, @Nullable ResourceLocation equipped, List<ResourceLocation> hats) {
        this.owner = owner;
        this.equipped = equipped;
        this.hats = List.copyOf(hats);
    }

    public UUID owner() {
        return owner;
    }

    @Nullable
    public ResourceLocation equipped() {
        return equipped;
    }

    public List<ResourceLocation> hats() {
        return hats;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketPeerInventory msg) {
        buf.writeUUID(msg.owner);
        buf.writeBoolean(msg.equipped != null);
        if (msg.equipped != null) {
            buf.writeResourceLocation(msg.equipped);
        }
        TradePacketBuffers.writeIds(buf, msg.hats);
    }

    public static PacketPeerInventory decode(RegistryFriendlyByteBuf buf) {
        UUID owner = buf.readUUID();
        ResourceLocation equipped = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new PacketPeerInventory(owner, equipped, TradePacketBuffers.readIds(buf));
    }

    public static void handle(PacketPeerInventory msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.trade.ClientTradePackets",
                "onPeerInventory",
                new Class<?>[]{PacketPeerInventory.class},
                new Object[]{msg}));
        
    }
}
