package com.astryxion.hats.client.network;

import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketEquipHat;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class HatClientPackets {

    private static final byte ID_EQUIP = 0;
    private static final byte ID_OPEN_COSMETIC = 1;
    private static final byte ID_OPEN_HUNTING = 2;

    private HatClientPackets() {}

    public static void sendEquipToServer(String hatId) {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        PacketEquipHat.encode(new PacketEquipHat(hatId), out);
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        ClientPacketDistributor.sendToServer(new HatPacketHandler.HatsC2SPayload(ID_EQUIP, arr));
    }

    public static void sendOpenCosmeticToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        ClientPacketDistributor.sendToServer(new HatPacketHandler.HatsC2SPayload(ID_OPEN_COSMETIC, arr));
    }

    public static void sendOpenHuntingToServer() {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        byte[] arr = new byte[out.readableBytes()];
        out.getBytes(out.readerIndex(), arr);
        ClientPacketDistributor.sendToServer(new HatPacketHandler.HatsC2SPayload(ID_OPEN_HUNTING, arr));
    }
}
