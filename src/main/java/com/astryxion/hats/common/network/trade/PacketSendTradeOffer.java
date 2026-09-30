package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.capability.HatDataAccess;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.trade.TradeErrorType;
import com.astryxion.hats.common.trade.TradeHats;
import com.astryxion.hats.common.trade.TradeOffer;
import com.astryxion.hats.common.trade.TradeOfferStore;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketSendTradeOffer {

    private final UUID receiverUuid;
    private final List<ResourceLocation> senderHats;
    private final List<ResourceLocation> receiverHats;

    public PacketSendTradeOffer(UUID receiverUuid, List<ResourceLocation> senderHats, List<ResourceLocation> receiverHats) {
        this.receiverUuid = receiverUuid;
        this.senderHats = List.copyOf(senderHats);
        this.receiverHats = List.copyOf(receiverHats);
    }

    public static void encode(PacketSendTradeOffer msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.receiverUuid);
        TradePacketBuffers.writeIds(buf, msg.senderHats);
        TradePacketBuffers.writeIds(buf, msg.receiverHats);
    }

    public static PacketSendTradeOffer decode(FriendlyByteBuf buf) {
        return new PacketSendTradeOffer(buf.readUUID(), TradePacketBuffers.readIds(buf), TradePacketBuffers.readIds(buf));
    }

    public static void handle(PacketSendTradeOffer msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null || sender.getServer() == null) {
                return;
            }
            ServerPlayer receiver = sender.getServer().getPlayerList().getPlayer(msg.receiverUuid);
            if (receiver == null || receiver.level() != sender.level()) {
                HatTradeNetwork.send(sender, new PacketTradeError(TradeErrorType.RECEIVER_OFFLINE, ""));
                return;
            }
            if (msg.senderHats.isEmpty() && msg.receiverHats.isEmpty()) {
                HatTradeNetwork.send(sender, new PacketTradeError(TradeErrorType.EMPTY_OFFER, ""));
                return;
            }
            if (!TradeHats.inRange(sender, receiver)) {
                HatTradeNetwork.send(sender, new PacketTradeError(TradeErrorType.OUT_OF_RANGE, ""));
                return;
            }

            if (HatDataAccess.all(sender).isEmpty() || HatDataAccess.all(receiver).isEmpty()) {
                HatTradeNetwork.send(sender, new PacketTradeError(TradeErrorType.RECEIVER_OFFLINE, ""));
                return;
            }

            if (!validateOwned(sender, sender, msg.senderHats, true)
                    || !validateOwned(sender, receiver, msg.receiverHats, false)) {
                return;
            }

            TradeOffer offer = new TradeOffer(
                    UUID.randomUUID(),
                    sender.getUUID(),
                    sender.getGameProfile().getName(),
                    receiver.getUUID(),
                    receiver.getGameProfile().getName(),
                    msg.senderHats,
                    msg.receiverHats,
                    System.currentTimeMillis()
            );
            TradeOffer replaced = TradeOfferStore.get().addOrReplace(offer);
            if (replaced != null) {
                HatTradeNetwork.send(receiver, new PacketTradeOfferRevoked(replaced.offerId));
            }
            TradeOfferStore.get().closeSession(sender.getUUID());
            HatTradeNetwork.send(receiver, new PacketIncomingTradeOffer(offer));
        });
        context.setPacketHandled(true);
    }

    private static boolean validateOwned(
            ServerPlayer notify,
            ServerPlayer owner,
            List<ResourceLocation> hats,
            boolean senderSide
    ) {
        for (ResourceLocation id : hats) {
            if (!TradeHats.isHat(id)
                    || (Config.hatMode != HatMode.COSMETIC && !HatDataAccess.has(owner, id))) {
                HatTradeNetwork.send(notify, new PacketTradeError(
                        senderSide ? TradeErrorType.SENDER_MISSING_HAT : TradeErrorType.RECEIVER_MISSING_HAT,
                        id.toString()));
                return false;
            }
        }
        return true;
    }
}
