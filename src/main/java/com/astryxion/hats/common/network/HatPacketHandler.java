package com.astryxion.hats.common.network;

import com.astryxion.hats.AstryxionsHats;
import com.astryxion.hats.common.hat.HatPartCapability;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class HatPacketHandler {

    public static final ResourceLocation CHANNEL_ID =
            new ResourceLocation(AstryxionsHats.MODID, "main");

    private static final byte ID_EQUIP = 0;
    private static final byte ID_OPEN_COSMETIC = 1;
    private static final byte ID_OPEN_HUNTING = 2;
    private static final byte ID_SYNC_HAT = 3;
    private static final byte ID_HAT_UNLOCKED = 4;
    private static final byte ID_SYNC_HAT_PART = 5;

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL_ID, (server, player, handler, buf, responseSender) -> {
            byte type = buf.readByte();
            FriendlyByteBuf copy = new FriendlyByteBuf(buf.copy());
            server.execute(() -> {
                try {
                    switch (type) {
                        case ID_EQUIP -> {
                            PacketEquipHat msg = PacketEquipHat.decode(copy);
                            PacketEquipHat.handle(msg, player);
                        }
                        case ID_OPEN_COSMETIC -> {
                            PacketOpenCosmeticMenu msg = new PacketOpenCosmeticMenu();
                            PacketOpenCosmeticMenu.handle(msg, player);
                        }
                        case ID_OPEN_HUNTING -> {
                            PacketOpenHuntingMenu msg = new PacketOpenHuntingMenu();
                            PacketOpenHuntingMenu.handle(msg, player);
                        }
                        default -> {}
                    }
                } finally {
                    copy.release();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CHANNEL_ID, (client, handler, buf, responseSender) -> {
            byte type = buf.readByte();
            FriendlyByteBuf copy = new FriendlyByteBuf(buf.copy());
            client.execute(() -> {
                try {
                    switch (type) {
                        case ID_SYNC_HAT -> {
                            PacketSyncHat msg = PacketSyncHat.decode(copy);
                            PacketSyncHat.handle(msg);
                        }
                        case ID_HAT_UNLOCKED -> {
                            PacketHatUnlocked msg = PacketHatUnlocked.decode(copy);
                            PacketHatUnlocked.handle(msg);
                        }
                        case ID_SYNC_HAT_PART -> {
                            PacketSyncHatPart msg = PacketSyncHatPart.decode(copy);
                            PacketSyncHatPart.handle(msg);
                        }
                        default -> {}
                    }
                } finally {
                    copy.release();
                }
            });
        });
    }

    public static void sendToPlayer(ServerPlayer player, byte packetId, FriendlyByteBuf buf) {
        if (ServerPlayNetworking.canSend(player, CHANNEL_ID)) {
            FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
            out.writeByte(packetId);
            out.writeBytes(buf);
            ServerPlayNetworking.send(player, CHANNEL_ID, out);
        }
    }

    public static void sendEquipToServer(String hatId) {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_EQUIP);
        PacketEquipHat.encode(new PacketEquipHat(hatId), out);
        ClientPlayNetworking.send(CHANNEL_ID, out);
    }

    public static void sendOpenCosmeticToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_OPEN_COSMETIC);
        ClientPlayNetworking.send(CHANNEL_ID, out);
    }

    public static void sendOpenHuntingToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_OPEN_HUNTING);
        ClientPlayNetworking.send(CHANNEL_ID, out);
    }

    public static void sendSyncHatToPlayer(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketSyncHat.encode(new PacketSyncHat(data), buf);
        sendToPlayer(player, ID_SYNC_HAT, buf);
    }

    public static void sendHatUnlockedToPlayer(ServerPlayer player, net.minecraft.world.item.ItemStack hatStack) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketHatUnlocked.encode(new PacketHatUnlocked(hatStack), buf);
        sendToPlayer(player, ID_HAT_UNLOCKED, buf);
    }

    public static void sendSyncHatPartToPlayer(ServerPlayer player, int entityId, net.minecraft.nbt.CompoundTag tag) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PacketSyncHatPart.encode(new PacketSyncHatPart(entityId, tag), buf);
        sendToPlayer(player, ID_SYNC_HAT_PART, buf);
    }

    /**
     * Syncs this player's equipped hat render state to every client that is tracking them, including their own.
     * Required for multiplayer and for the local client on a dedicated server after equip.
     */
    public static void syncPlayerHatPartToTracking(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }
        var part = HatPartCapability.getOrCreate(player);
        var tag = part.serializeNBT();
        int entityId = player.getId();
        for (ServerPlayer viewer : PlayerLookup.tracking(player)) {
            sendSyncHatPartToPlayer(viewer, entityId, tag);
        }
        sendSyncHatPartToPlayer(player, entityId, tag);
    }

    /**
     * Sends one player's hat render state to a single observer (e.g. when they start tracking that player).
     */
    public static void sendPlayerHatPartTo(ServerPlayer observer, Player hatOwner) {
        if (observer == null || hatOwner == null || observer.level().isClientSide()) {
            return;
        }
        var part = HatPartCapability.getOrCreate(hatOwner);
        sendSyncHatPartToPlayer(observer, hatOwner.getId(), part.serializeNBT());
    }
}
