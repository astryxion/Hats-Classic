package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.common.capability.HatDataAccess;
import com.astryxion.hats.common.trade.TradeBuilderSession;
import com.astryxion.hats.common.trade.TradeErrorType;
import com.astryxion.hats.common.trade.TradeHats;
import com.astryxion.hats.common.trade.TradeOfferStore;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketRequestPeerInventory {

    private final UUID targetUuid;

    public PacketRequestPeerInventory(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    public static void encode(PacketRequestPeerInventory msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.targetUuid);
    }

    public static PacketRequestPeerInventory decode(FriendlyByteBuf buf) {
        return new PacketRequestPeerInventory(buf.readUUID());
    }

    public static void handle(PacketRequestPeerInventory msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
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
        context.setPacketHandled(true);
    }
}
