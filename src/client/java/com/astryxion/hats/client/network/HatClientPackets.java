package com.astryxion.hats.client.network;

import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketEquipHat;
import com.astryxion.hats.common.network.PacketHatUnlocked;
import com.astryxion.hats.common.network.PacketSyncHat;
import com.astryxion.hats.common.network.PacketSyncHatPart;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public final class HatClientPackets {

    private static final byte ID_EQUIP = 0;
    private static final byte ID_OPEN_COSMETIC = 1;
    private static final byte ID_OPEN_HUNTING = 2;
    private static final byte ID_SYNC_HAT = 3;
    private static final byte ID_HAT_UNLOCKED = 4;
    private static final byte ID_SYNC_HAT_PART = 5;

    private HatClientPackets() {}

    public static void registerS2CReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(HatPacketHandler.HatsS2CPayload.TYPE, (payload, context) -> {
            FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.payload()));
            try {
                switch (payload.typeId()) {
                    case ID_SYNC_HAT -> {
                        PacketSyncHat msg = PacketSyncHat.decode(copy);
                        PacketSyncHat.handle(msg);
                        HatClientSync.applyLocalEquippedHat();
                    }
                    case ID_HAT_UNLOCKED -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.level == null) return;
                        var registryAccess = mc.level.registryAccess();
                        PacketHatUnlocked msg = PacketHatUnlocked.decode(copy, registryAccess);
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
    }

    public static void sendEquipToServer(String hatId) {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        PacketEquipHat.encode(new PacketEquipHat(hatId), out);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        ClientPlayNetworking.send(new HatPacketHandler.HatsC2SPayload(ID_EQUIP, arr));
    }

    public static void sendOpenCosmeticToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_OPEN_COSMETIC);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        ClientPlayNetworking.send(new HatPacketHandler.HatsC2SPayload(ID_OPEN_COSMETIC, arr));
    }

    public static void sendOpenHuntingToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeByte(ID_OPEN_HUNTING);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        ClientPlayNetworking.send(new HatPacketHandler.HatsC2SPayload(ID_OPEN_HUNTING, arr));
    }
}
