package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenCosmeticMenu {

    public PacketOpenCosmeticMenu() {
        // No data needed
    }

    public PacketOpenCosmeticMenu(FriendlyByteBuf buffer) {
        // Empty constructor for bytebuf
    }

    public void toBytes(FriendlyByteBuf buffer) {
        // No data to write
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();

            // 🛑 All advancement logic has been removed to prevent the "Pink Checkerboard" tab bug.
            if (player != null && !player.isCreative()) {
                // The menu opens purely for cosmetic purposes now.
                // No more "Config Demon" or ghost tabs.
            }
        });
        return true;
    }
}