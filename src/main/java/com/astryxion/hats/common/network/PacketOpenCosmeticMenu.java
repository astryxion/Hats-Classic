package com.astryxion.hats.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.astryxion.hats.Hats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class PacketOpenCosmeticMenu implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketOpenCosmeticMenu> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "open_cosmetic_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenCosmeticMenu> STREAM_CODEC =
            StreamCodec.of(PacketOpenCosmeticMenu::encode, PacketOpenCosmeticMenu::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public PacketOpenCosmeticMenu() {
        // No data needed
    }

    public static void encode(RegistryFriendlyByteBuf buffer, PacketOpenCosmeticMenu msg) {
        // No data to write
    }

    public static PacketOpenCosmeticMenu decode(RegistryFriendlyByteBuf buffer) {
        return new PacketOpenCosmeticMenu();
    }

    public static void handle(PacketOpenCosmeticMenu msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            // All advancement logic has been removed to prevent the "Pink Checkerboard" tab bug.
            if (player != null && !player.isCreative()) {
                // The menu opens purely for cosmetic purposes now.
                // No more "Config Demon" or ghost tabs.
            }
        });
    }
}