package com.astryxion.hats.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.hat.HatAdvancements;
import com.astryxion.hats.common.hat.HatMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Supplier;

public class PacketOpenHuntingMenu implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketOpenHuntingMenu> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "open_hunting_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenHuntingMenu> STREAM_CODEC =
            StreamCodec.of(PacketOpenHuntingMenu::encode, PacketOpenHuntingMenu::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public PacketOpenHuntingMenu() {}

    public static void encode(RegistryFriendlyByteBuf buffer, PacketOpenHuntingMenu msg) {}

    public static PacketOpenHuntingMenu decode(RegistryFriendlyByteBuf buffer) {
        return new PacketOpenHuntingMenu();
    }

    public static void handle(PacketOpenHuntingMenu msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player != null
                    && !player.isCreative()
                    && !player.isSpectator()
                    && Config.hatMode == HatMode.HUNTING) {
                HatAdvancements.award(player, "hats/hunt_begin");
            }
        });
    }
}