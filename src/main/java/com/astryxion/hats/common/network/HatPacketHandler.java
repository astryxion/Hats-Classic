package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatPart;
import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.network.trade.PacketCloseTradeBuilder;
import com.astryxion.hats.common.network.trade.PacketIncomingTradeOffer;
import com.astryxion.hats.common.network.trade.PacketNearbyPlayersList;
import com.astryxion.hats.common.network.trade.PacketPeerInventory;
import com.astryxion.hats.common.network.trade.PacketRequestNearbyPlayers;
import com.astryxion.hats.common.network.trade.PacketRequestPeerInventory;
import com.astryxion.hats.common.network.trade.PacketRespondTradeOffer;
import com.astryxion.hats.common.network.trade.PacketSendTradeOffer;
import com.astryxion.hats.common.network.trade.PacketTradeError;
import com.astryxion.hats.common.network.trade.PacketTradeOfferRevoked;
import com.astryxion.hats.common.network.trade.PacketTradePartnerLeft;
import com.astryxion.hats.common.network.trade.PacketTradeResolved;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Hats.MODID)
public class HatPacketHandler {

    private static final String CLIENT_HAT_HANDLERS = "com.astryxion.hats.client.network.HatClientPacketHandlers";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("2");

        registrar.playToServer(PacketEquipHat.TYPE, PacketEquipHat.STREAM_CODEC, PacketEquipHat::handle);
        registrar.playToServer(PacketOpenCosmeticMenu.TYPE, PacketOpenCosmeticMenu.STREAM_CODEC, PacketOpenCosmeticMenu::handle);
        registrar.playToServer(PacketOpenHuntingMenu.TYPE, PacketOpenHuntingMenu.STREAM_CODEC, PacketOpenHuntingMenu::handle);
        registrar.playToServer(PacketUpdateHatStand.TYPE, PacketUpdateHatStand.STREAM_CODEC, PacketUpdateHatStand::handle);
        registrar.playToServer(PacketSetLauncherHat.TYPE, PacketSetLauncherHat.STREAM_CODEC, PacketSetLauncherHat::handle);

        registrar.playToClient(PacketSyncHat.TYPE, PacketSyncHat.STREAM_CODEC, PacketSyncHat::handle);
        registrar.playToClient(PacketHatUnlocked.TYPE, PacketHatUnlocked.STREAM_CODEC, HatPacketHandler::handleHatUnlocked);
        registrar.playToClient(PacketSyncHatPart.TYPE, PacketSyncHatPart.STREAM_CODEC, PacketSyncHatPart::handle);
        registrar.playToClient(PacketOpenHatStand.TYPE, PacketOpenHatStand.STREAM_CODEC, PacketOpenHatStand::handle);

        registerTradePackets(registrar);
    }

    private static void registerTradePackets(PayloadRegistrar registrar) {
        registrar.playToServer(PacketRequestNearbyPlayers.TYPE, PacketRequestNearbyPlayers.STREAM_CODEC, PacketRequestNearbyPlayers::handle);
        registrar.playToServer(PacketRequestPeerInventory.TYPE, PacketRequestPeerInventory.STREAM_CODEC, PacketRequestPeerInventory::handle);
        registrar.playToServer(PacketSendTradeOffer.TYPE, PacketSendTradeOffer.STREAM_CODEC, PacketSendTradeOffer::handle);
        registrar.playToServer(PacketRespondTradeOffer.TYPE, PacketRespondTradeOffer.STREAM_CODEC, PacketRespondTradeOffer::handle);
        registrar.playToServer(PacketCloseTradeBuilder.TYPE, PacketCloseTradeBuilder.STREAM_CODEC, PacketCloseTradeBuilder::handle);

        registrar.playToClient(PacketNearbyPlayersList.TYPE, PacketNearbyPlayersList.STREAM_CODEC, PacketNearbyPlayersList::handle);
        registrar.playToClient(PacketPeerInventory.TYPE, PacketPeerInventory.STREAM_CODEC, PacketPeerInventory::handle);
        registrar.playToClient(PacketIncomingTradeOffer.TYPE, PacketIncomingTradeOffer.STREAM_CODEC, PacketIncomingTradeOffer::handle);
        registrar.playToClient(PacketTradeResolved.TYPE, PacketTradeResolved.STREAM_CODEC, PacketTradeResolved::handle);
        registrar.playToClient(PacketTradeOfferRevoked.TYPE, PacketTradeOfferRevoked.STREAM_CODEC, PacketTradeOfferRevoked::handle);
        registrar.playToClient(PacketTradePartnerLeft.TYPE, PacketTradePartnerLeft.STREAM_CODEC, PacketTradePartnerLeft::handle);
        registrar.playToClient(PacketTradeError.TYPE, PacketTradeError.STREAM_CODEC, PacketTradeError::handle);
        registrar.playToClient(PacketSetUnlockedHats.TYPE, PacketSetUnlockedHats.STREAM_CODEC, PacketSetUnlockedHats::handle);
        registrar.playToClient(PacketRehatify.TYPE, PacketRehatify.STREAM_CODEC, PacketRehatify::handle);
    }

    private static void handleHatUnlocked(PacketHatUnlocked msg, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> callClient(CLIENT_HAT_HANDLERS, "handleHatUnlocked",
                new Class<?>[]{PacketHatUnlocked.class}, new Object[]{msg}));
    }

    /**
     * Loads a client class by name only when this process is a physical client.
     * A direct reference would put {@code LocalPlayer} on the dedicated-server classpath.
     */
    public static void callClient(String className, String method, Class<?>[] types, Object[] args) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        try {
            Class.forName(className).getMethod(method, types).invoke(null, args);
        } catch (Throwable t) {
            Hats.LOGGER.error("Failed to handle client packet {}.{}", className, method, t);
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload msg) {
        PacketDistributor.sendToPlayer(player, msg);
    }

    public static void sendToTrackingAndSelf(LivingEntity entity, CustomPacketPayload msg) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, msg);
    }

    /**
     * Client to server. PacketDistributor.sendToServer is client-side, so this
     * returns immediately on a dedicated server.
     */
    public static void sendToServer(CustomPacketPayload msg) {
        if (FMLEnvironment.dist != Dist.CLIENT || msg == null) {
            return;
        }
        PacketDistributor.sendToServer(msg);
    }

    public static void syncHatPartToTracking(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        HatPart part = entity.getData(HatPartCapability.HAT_PART);
        sendToTrackingAndSelf(entity, new PacketSyncHatPart(entity.getId(), part.serializeNBT()));
    }

    public static void syncPlayerHatPartToTracking(ServerPlayer player) {
        syncHatPartToTracking(player);
    }

    public static void sendHatPartTo(ServerPlayer observer, LivingEntity hatOwner) {
        if (observer == null || hatOwner == null || observer.level().isClientSide()) {
            return;
        }
        HatPart part = hatOwner.getData(HatPartCapability.HAT_PART);
        sendToPlayer(observer, new PacketSyncHatPart(hatOwner.getId(), part.serializeNBT()));
    }

    public static void sendPlayerHatPartTo(ServerPlayer observer, Player hatOwner) {
        sendHatPartTo(observer, hatOwner);
    }
}
