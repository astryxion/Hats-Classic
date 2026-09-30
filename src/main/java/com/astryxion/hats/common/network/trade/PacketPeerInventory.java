package com.astryxion.hats.common.network.trade;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketPeerInventory {

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

    public static void encode(PacketPeerInventory msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.owner);
        buf.writeBoolean(msg.equipped != null);
        if (msg.equipped != null) {
            buf.writeResourceLocation(msg.equipped);
        }
        TradePacketBuffers.writeIds(buf, msg.hats);
    }

    public static PacketPeerInventory decode(FriendlyByteBuf buf) {
        UUID owner = buf.readUUID();
        ResourceLocation equipped = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new PacketPeerInventory(owner, equipped, TradePacketBuffers.readIds(buf));
    }

    public static void handle(PacketPeerInventory msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.astryxion.hats.client.trade.ClientTradePackets.onPeerInventory(msg)));
        ctx.get().setPacketHandled(true);
    }
}
