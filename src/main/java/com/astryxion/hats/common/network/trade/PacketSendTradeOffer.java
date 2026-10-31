package com.astryxion.hats.common.network.trade;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.capability.HatDataAccess;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.trade.TradeErrorType;
import com.astryxion.hats.common.trade.TradeHats;
import com.astryxion.hats.common.trade.TradeOffer;
import com.astryxion.hats.common.trade.TradeOfferStore;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public class PacketSendTradeOffer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSendTradeOffer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "send_trade_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSendTradeOffer> STREAM_CODEC =
            StreamCodec.of(PacketSendTradeOffer::encode, PacketSendTradeOffer::decode);

    private final UUID receiverUuid;
    private final List<Identifier> senderHats;
    private final List<Identifier> receiverHats;

    public PacketSendTradeOffer(UUID receiverUuid, List<Identifier> senderHats, List<Identifier> receiverHats) {
        this.receiverUuid = receiverUuid;
        this.senderHats = List.copyOf(senderHats);
        this.receiverHats = List.copyOf(receiverHats);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketSendTradeOffer msg) {
        buf.writeUUID(msg.receiverUuid);
        TradePacketBuffers.writeIds(buf, msg.senderHats);
        TradePacketBuffers.writeIds(buf, msg.receiverHats);
    }

    public static PacketSendTradeOffer decode(RegistryFriendlyByteBuf buf) {
        return new PacketSendTradeOffer(buf.readUUID(), TradePacketBuffers.readIds(buf), TradePacketBuffers.readIds(buf));
    }

    public static void handle(PacketSendTradeOffer msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) ctx.player();
            if (sender == null || sender.level().getServer() == null) {
                return;
            }
            ServerPlayer receiver = sender.level().getServer().getPlayerList().getPlayer(msg.receiverUuid);
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
                    sender.getGameProfile().name(),
                    receiver.getUUID(),
                    receiver.getGameProfile().name(),
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
    }

    private static boolean validateOwned(
            ServerPlayer notify,
            ServerPlayer owner,
            List<Identifier> hats,
            boolean senderSide
    ) {
        for (Identifier id : hats) {
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
