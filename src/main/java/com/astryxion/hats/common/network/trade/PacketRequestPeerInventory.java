package com.astryxion.hats.common.network.trade;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.astryxion.hats.common.capability.HatDataAccess;
import com.astryxion.hats.common.trade.TradeBuilderSession;
import com.astryxion.hats.common.trade.TradeErrorType;
import com.astryxion.hats.common.trade.TradeHats;
import com.astryxion.hats.common.trade.TradeOfferStore;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketRequestPeerInventory implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketRequestPeerInventory> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "request_peer_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestPeerInventory> STREAM_CODEC =
            StreamCodec.of(PacketRequestPeerInventory::encode, PacketRequestPeerInventory::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final UUID targetUuid;

    public PacketRequestPeerInventory(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketRequestPeerInventory msg) {
        buf.writeUUID(msg.targetUuid);
    }

    public static PacketRequestPeerInventory decode(RegistryFriendlyByteBuf buf) {
        return new PacketRequestPeerInventory(buf.readUUID());
    }

    public static void handle(PacketRequestPeerInventory msg, IPayloadContext ctx) {
        IPayloadContext context = ctx;
        context.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) context.player();
            if (sender == null || sender.getServer() == null) {
                return;
            }
            ServerPlayer target = sender.getServer().getPlayerList().getPlayer(msg.targetUuid);
            if (target == null || target.level() != sender.level()) {
                HatTradeNetwork.send(sender, new PacketTradeError(TradeErrorType.RECEIVER_OFFLINE, ""));
                return;
            }
            if (!TradeHats.inRange(sender, target)) {
                HatTradeNetwork.send(sender, new PacketTradeError(TradeErrorType.OUT_OF_RANGE, ""));
                return;
            }
            TradeOfferStore.get().openSession(new TradeBuilderSession(
                    sender.getUUID(), msg.targetUuid, System.currentTimeMillis()));
            List<ResourceLocation> hats = HatDataAccess.copyUnlocked(target);
            hats = hats.stream().filter(TradeHats::isHat).toList();
            ResourceLocation equipped = TradeHats.equippedId(target);
            HatTradeNetwork.send(sender, new PacketPeerInventory(msg.targetUuid, equipped, hats));
        });
    }
}
