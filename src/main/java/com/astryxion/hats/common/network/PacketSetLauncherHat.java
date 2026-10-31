package com.astryxion.hats.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.astryxion.hats.common.launcher.HatLaunch;
import com.astryxion.hats.common.launcher.HatLauncherItem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class PacketSetLauncherHat implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketSetLauncherHat> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "set_launcher_hat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSetLauncherHat> STREAM_CODEC =
            StreamCodec.of(PacketSetLauncherHat::encode, PacketSetLauncherHat::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final String hatId;
    private final InteractionHand hand;

    public PacketSetLauncherHat(String hatId, InteractionHand hand) {
        this.hatId = hatId == null ? HatLaunch.RANDOM_ID : hatId;
        this.hand = hand;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketSetLauncherHat msg) {
        buf.writeUtf(msg.hatId);
        buf.writeEnum(msg.hand);
    }

    public static PacketSetLauncherHat decode(RegistryFriendlyByteBuf buf) {
        return new PacketSetLauncherHat(buf.readUtf(), buf.readEnum(InteractionHand.class));
    }

    public static void handle(PacketSetLauncherHat msg, IPayloadContext ctx) {
        IPayloadContext context = ctx;
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) {
                return;
            }
            ItemStack stack = player.getItemInHand(msg.hand);
            if (stack.getItem() instanceof HatLauncherItem) {
                HatLaunch.storeId(stack, msg.hatId);
                player.getInventory().setChanged();
            }
        });
    }
}
