package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class HatPacketHandler {

    private static final String CLIENT_HAT_HANDLERS = "com.astryxion.hats.client.network.HatClientPacketHandlers";

    private static final String PROTOCOL = "2";
    private static int id = 0;

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(Hats.MODID, "main"),
                    () -> PROTOCOL,
                    PROTOCOL::equals,
                    PROTOCOL::equals
            );

    public static void register() {

        // =====================
        // CLIENT -> SERVER
        // =====================

        // Handles equipping hats
        CHANNEL.registerMessage(
                id++,
                PacketEquipHat.class,
                PacketEquipHat::encode,
                PacketEquipHat::decode,
                PacketEquipHat::handle
        );

        // 🔧 NEW: Handles granting "The Config Demon" (Cosmetic Mode)
        CHANNEL.registerMessage(
                id++,
                PacketOpenCosmeticMenu.class,
                PacketOpenCosmeticMenu::toBytes,
                PacketOpenCosmeticMenu::new,
                PacketOpenCosmeticMenu::handle
        );

        // 🔧 NEW: Handles granting "Let The Hunt Begin!" (Hunting Mode)
        CHANNEL.registerMessage(
                id++,
                PacketOpenHuntingMenu.class,
                PacketOpenHuntingMenu::toBytes,
                PacketOpenHuntingMenu::new,
                PacketOpenHuntingMenu::handle
        );

        CHANNEL.registerMessage(
                id++,
                PacketUpdateHatStand.class,
                PacketUpdateHatStand::encode,
                PacketUpdateHatStand::decode,
                PacketUpdateHatStand::handle
        );

        CHANNEL.registerMessage(
                id++,
                PacketOpenHatStand.class,
                PacketOpenHatStand::encode,
                PacketOpenHatStand::decode,
                PacketOpenHatStand::handle
        );

        CHANNEL.registerMessage(
                id++,
                PacketSetLauncherHat.class,
                PacketSetLauncherHat::encode,
                PacketSetLauncherHat::decode,
                PacketSetLauncherHat::handle
        );

        // =====================
        // SERVER -> CLIENT
        // =====================

        // Syncs equipped hat state to the client
        CHANNEL.registerMessage(
                id++,
                PacketSyncHat.class,
                PacketSyncHat::encode,
                PacketSyncHat::decode,
                PacketSyncHat::handle
        );

        CHANNEL.registerMessage(
                id++,
                PacketHatUnlocked.class,
                PacketHatUnlocked::encode,
                PacketHatUnlocked::decode,
                HatPacketHandler::handleHatUnlocked
        );

        // Syncs the actual hat model part for rendering
        CHANNEL.registerMessage(
                id++,
                PacketSyncHatPart.class,
                PacketSyncHatPart::encode,
                PacketSyncHatPart::decode,
                PacketSyncHatPart::handle
        );

        registerTradePackets();
    }

    private static void registerTradePackets() {
        CHANNEL.registerMessage(id++, PacketRequestNearbyPlayers.class, PacketRequestNearbyPlayers::encode, PacketRequestNearbyPlayers::decode, PacketRequestNearbyPlayers::handle);
        CHANNEL.registerMessage(id++, PacketRequestPeerInventory.class, PacketRequestPeerInventory::encode, PacketRequestPeerInventory::decode, PacketRequestPeerInventory::handle);
        CHANNEL.registerMessage(id++, PacketSendTradeOffer.class, PacketSendTradeOffer::encode, PacketSendTradeOffer::decode, PacketSendTradeOffer::handle);
        CHANNEL.registerMessage(id++, PacketRespondTradeOffer.class, PacketRespondTradeOffer::encode, PacketRespondTradeOffer::decode, PacketRespondTradeOffer::handle);
        CHANNEL.registerMessage(id++, PacketCloseTradeBuilder.class, PacketCloseTradeBuilder::encode, PacketCloseTradeBuilder::decode, PacketCloseTradeBuilder::handle);

        CHANNEL.registerMessage(id++, PacketNearbyPlayersList.class, PacketNearbyPlayersList::encode, PacketNearbyPlayersList::decode, PacketNearbyPlayersList::handle);
        CHANNEL.registerMessage(id++, PacketPeerInventory.class, PacketPeerInventory::encode, PacketPeerInventory::decode, PacketPeerInventory::handle);
        CHANNEL.registerMessage(id++, PacketIncomingTradeOffer.class, PacketIncomingTradeOffer::encode, PacketIncomingTradeOffer::decode, PacketIncomingTradeOffer::handle);
        CHANNEL.registerMessage(id++, PacketTradeResolved.class, PacketTradeResolved::encode, PacketTradeResolved::decode, PacketTradeResolved::handle);
        CHANNEL.registerMessage(id++, PacketTradeOfferRevoked.class, PacketTradeOfferRevoked::encode, PacketTradeOfferRevoked::decode, PacketTradeOfferRevoked::handle);
        CHANNEL.registerMessage(id++, PacketTradePartnerLeft.class, PacketTradePartnerLeft::encode, PacketTradePartnerLeft::decode, PacketTradePartnerLeft::handle);
        CHANNEL.registerMessage(id++, PacketTradeError.class, PacketTradeError::encode, PacketTradeError::decode, PacketTradeError::handle);
        CHANNEL.registerMessage(id++, PacketSetUnlockedHats.class, PacketSetUnlockedHats::encode, PacketSetUnlockedHats::decode, PacketSetUnlockedHats::handle);
        CHANNEL.registerMessage(id++, PacketRehatify.class, PacketRehatify::encode, PacketRehatify::decode, PacketRehatify::handle);
    }

    /**
     * Invoked only on the physical client when the S2C packet is received; uses reflection so this class has no
     * compile-time dependency on client types (dedicated server must not load Toast / Minecraft.gui).
     */
    private static void handleHatUnlocked(PacketHatUnlocked msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            try {
                Class.forName(CLIENT_HAT_HANDLERS)
                        .getMethod("handleHatUnlocked", PacketHatUnlocked.class)
                        .invoke(null, msg);
            } catch (Throwable t) {
                Hats.LOGGER.error("Failed to handle hat unlocked packet on client", t);
            }
        });
        context.setPacketHandled(true);
    }

    // =====================
    // Helper
    // =====================

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                msg
        );
    }

    /**
     * Syncs this entity's hat to every client that can see them. Players also
     * receive their own copy. Hats never use the helmet slot as a courier.
     */
    public static void syncHatPartToTracking(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        entity.getCapability(HatPartCapability.HAT_PART).ifPresent(part ->
                CHANNEL.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new PacketSyncHatPart(entity.getId(), part.serializeNBT())
                ));
    }

    public static void syncPlayerHatPartToTracking(ServerPlayer player) {
        syncHatPartToTracking(player);
    }

    /**
     * Sends one entity's hat state to a single observer (e.g. when they start tracking).
     */
    public static void sendHatPartTo(ServerPlayer observer, LivingEntity hatOwner) {
        if (observer == null || hatOwner == null || observer.level().isClientSide()) {
            return;
        }
        hatOwner.getCapability(HatPartCapability.HAT_PART).ifPresent(part ->
                sendToPlayer(observer, new PacketSyncHatPart(hatOwner.getId(), part.serializeNBT())));
    }

    public static void sendPlayerHatPartTo(ServerPlayer observer, Player hatOwner) {
        sendHatPartTo(observer, hatOwner);
    }
}