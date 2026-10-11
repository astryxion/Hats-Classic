package com.astryxion.hats.common.network;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.hat.HatAdvancements;
import com.astryxion.hats.common.hat.HatMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketOpenHuntingMenu implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketOpenHuntingMenu> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "open_hunting_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenHuntingMenu> STREAM_CODEC =
            StreamCodec.of(PacketOpenHuntingMenu::encode, PacketOpenHuntingMenu::decode);

    public PacketOpenHuntingMenu() {}

    public PacketOpenHuntingMenu(FriendlyByteBuf buffer) {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketOpenHuntingMenu msg) {
        msg.toBytes(buf);
    }

    public static PacketOpenHuntingMenu decode(RegistryFriendlyByteBuf buf) {
        return new PacketOpenHuntingMenu(buf);
    }

    public void toBytes(FriendlyByteBuf buffer) {}

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
