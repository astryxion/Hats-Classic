package com.astryxion.hats.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketOpenCosmeticMenu implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketOpenCosmeticMenu> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "open_cosmetic_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenCosmeticMenu> STREAM_CODEC =
            StreamCodec.of(PacketOpenCosmeticMenu::encode, PacketOpenCosmeticMenu::decode);

    public PacketOpenCosmeticMenu() {
        // No data needed
    }

    public PacketOpenCosmeticMenu(FriendlyByteBuf buffer) {
        // Empty constructor for bytebuf
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketOpenCosmeticMenu msg) {
        msg.toBytes(buf);
    }

    public static PacketOpenCosmeticMenu decode(RegistryFriendlyByteBuf buf) {
        return new PacketOpenCosmeticMenu(buf);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        // No data to write
    }

    public static void handle(PacketOpenCosmeticMenu msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            // 🛑 All advancement logic has been removed to prevent the "Pink Checkerboard" tab bug.
            if (player != null && !player.isCreative()) {
                // The menu opens purely for cosmetic purposes now.
                // No more "Config Demon" or ghost tabs.
            }
        });
    }
}
