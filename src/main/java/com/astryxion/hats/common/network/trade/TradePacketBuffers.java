package com.astryxion.hats.common.network.trade;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

final class TradePacketBuffers {

    private TradePacketBuffers() {}

    static void writeIds(FriendlyByteBuf buf, List<ResourceLocation> ids) {
        buf.writeVarInt(ids.size());
        for (ResourceLocation id : ids) {
            buf.writeResourceLocation(id);
        }
    }

    static List<ResourceLocation> readIds(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<ResourceLocation> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(buf.readResourceLocation());
        }
        return ids;
    }
}
