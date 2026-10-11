package com.astryxion.hats.common.network.trade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PacketTradeResolved implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketTradeResolved> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "trade_resolved"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTradeResolved> STREAM_CODEC =
            StreamCodec.of(PacketTradeResolved::encode, PacketTradeResolved::decode);

    private final UUID offerId;
    private final boolean accepted;
    private final String otherName;
    private final List<Identifier> unlocked;
    private final List<Identifier> gained;
    private final List<Identifier> lost;
    @Nullable
    private final Identifier equipped;

    public PacketTradeResolved(UUID offerId, boolean accepted, String otherName) {
        this(offerId, accepted, otherName, List.of(), List.of(), List.of(), null);
    }

    public PacketTradeResolved(
            UUID offerId,
            boolean accepted,
            String otherName,
            List<Identifier> unlocked,
            List<Identifier> gained,
            List<Identifier> lost,
            @Nullable Identifier equipped
    ) {
        this.offerId = offerId;
        this.accepted = accepted;
        this.otherName = otherName;
        this.unlocked = List.copyOf(unlocked);
        this.gained = List.copyOf(gained);
        this.lost = List.copyOf(lost);
        this.equipped = equipped;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
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

    public List<Identifier> unlocked() {
        return unlocked;
    }

    public List<Identifier> gained() {
        return gained;
    }

    public List<Identifier> lost() {
        return lost;
    }

    @Nullable
    public Identifier equipped() {
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
                buf.writeIdentifier(msg.equipped);
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
        List<Identifier> unlocked = TradePacketBuffers.readIds(buf);
        List<Identifier> gained = TradePacketBuffers.readIds(buf);
        List<Identifier> lost = TradePacketBuffers.readIds(buf);
        Identifier equipped = buf.readBoolean() ? buf.readIdentifier() : null;
        return new PacketTradeResolved(offerId, accepted, otherName, unlocked, gained, lost, equipped);
    }

    public static void handle(PacketTradeResolved msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (net.neoforged.fml.loading.FMLEnvironment.getDist() != Dist.CLIENT) {
                return;
            }
            com.astryxion.hats.client.trade.ClientTradePackets.onResolved(msg);
        });
    }
}
