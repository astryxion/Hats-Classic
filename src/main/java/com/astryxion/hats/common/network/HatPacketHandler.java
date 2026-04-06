package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatPartCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

    private static final String PROTOCOL = "1";
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
     * Syncs this player's equipped hat (render capability) to every client that can see them, including their own.
     * Required for multiplayer so other players see the correct hat model.
     */
    public static void syncPlayerHatPartToTracking(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }
        player.getCapability(HatPartCapability.HAT_PART).ifPresent(part ->
                CHANNEL.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new PacketSyncHatPart(player.getId(), part.serializeNBT())
                ));
    }

    /**
     * Sends one player's hat state to a single observer (e.g. when they start tracking that player).
     */
    public static void sendPlayerHatPartTo(ServerPlayer observer, Player hatOwner) {
        if (observer == null || hatOwner == null || observer.level().isClientSide()) {
            return;
        }
        hatOwner.getCapability(HatPartCapability.HAT_PART).ifPresent(part ->
                sendToPlayer(observer, new PacketSyncHatPart(hatOwner.getId(), part.serializeNBT())));
    }
}