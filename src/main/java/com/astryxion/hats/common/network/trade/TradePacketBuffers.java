package com.astryxion.hats.common.network.trade;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

final class TradePacketBuffers {

    private TradePacketBuffers() {}

    static void writeIds(FriendlyByteBuf buf, List<Identifier> ids) {
        buf.writeVarInt(ids.size());
        for (Identifier id : ids) {
            buf.writeIdentifier(id);
        }
    }

    static List<Identifier> readIds(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Identifier> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(buf.readIdentifier());
        }
        return ids;
    }
}
