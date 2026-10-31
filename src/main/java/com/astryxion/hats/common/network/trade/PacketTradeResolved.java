package com.astryxion.hats.common.network.trade;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import com.astryxion.hats.common.network.HatPacketHandler;


import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketTradeResolved implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketTradeResolved> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "trade_resolved"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTradeResolved> STREAM_CODEC =
            StreamCodec.of(PacketTradeResolved::encode, PacketTradeResolved::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final UUID offerId;
    private final boolean accepted;
    private final String otherName;
    private final List<ResourceLocation> unlocked;
    private final List<ResourceLocation> gained;
    private final List<ResourceLocation> lost;
    @Nullable
    private final ResourceLocation equipped;

    public PacketTradeResolved(UUID offerId, boolean accepted, String otherName) {
        this(offerId, accepted, otherName, List.of(), List.of(), List.of(), null);
    }

    public PacketTradeResolved(
            UUID offerId,
            boolean accepted,
            String otherName,
            List<ResourceLocation> unlocked,
            List<ResourceLocation> gained,
            List<ResourceLocation> lost,
            @Nullable ResourceLocation equipped
    ) {
        this.offerId = offerId;
        this.accepted = accepted;
        this.otherName = otherName;
        this.unlocked = List.copyOf(unlocked);
        this.gained = List.copyOf(gained);
        this.lost = List.copyOf(lost);
        this.equipped = equipped;
    }

    public UUID offerId() {
        return offerId;
    }

    public boolean accepted() {
        return accepted;
    }

    public String otherName() {
        return otherName;
    }

    public List<ResourceLocation> unlocked() {
        return unlocked;
    }

    public List<ResourceLocation> gained() {
        return gained;
    }

    public List<ResourceLocation> lost() {
        return lost;
    }

    @Nullable
    public ResourceLocation equipped() {
        return equipped;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketTradeResolved msg) {
        buf.writeUUID(msg.offerId);
        buf.writeBoolean(msg.accepted);
        buf.writeUtf(msg.otherName);
        buf.writeBoolean(msg.accepted);
        if (msg.accepted) {
            TradePacketBuffers.writeIds(buf, msg.unlocked);
            TradePacketBuffers.writeIds(buf, msg.gained);
            TradePacketBuffers.writeIds(buf, msg.lost);
            buf.writeBoolean(msg.equipped != null);
            if (msg.equipped != null) {
                buf.writeResourceLocation(msg.equipped);
            }
        }
    }

    public static PacketTradeResolved decode(RegistryFriendlyByteBuf buf) {
        UUID offerId = buf.readUUID();
        boolean accepted = buf.readBoolean();
        String otherName = buf.readUtf();
        boolean hasInventory = buf.readBoolean();
        if (!hasInventory) {
            return new PacketTradeResolved(offerId, accepted, otherName);
        }
        List<ResourceLocation> unlocked = TradePacketBuffers.readIds(buf);
        List<ResourceLocation> gained = TradePacketBuffers.readIds(buf);
        List<ResourceLocation> lost = TradePacketBuffers.readIds(buf);
        ResourceLocation equipped = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new PacketTradeResolved(offerId, accepted, otherName, unlocked, gained, lost, equipped);
    }

    public static void handle(PacketTradeResolved msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> HatPacketHandler.callClient(
                "com.astryxion.hats.client.trade.ClientTradePackets",
                "onResolved",
                new Class<?>[]{PacketTradeResolved.class},
                new Object[]{msg}));
        
    }
}
