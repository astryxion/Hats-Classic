package com.astryxion.hats.common.network;

import com.astryxion.hats.common.launcher.HatLaunch;
import com.astryxion.hats.common.launcher.HatLauncherItem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSetLauncherHat {

    private final String hatId;
    private final InteractionHand hand;

    public PacketSetLauncherHat(String hatId, InteractionHand hand) {
        this.hatId = hatId == null ? HatLaunch.RANDOM_ID : hatId;
        this.hand = hand;
    }

    public static void encode(PacketSetLauncherHat msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.hatId);
        buf.writeEnum(msg.hand);
    }

    public static PacketSetLauncherHat decode(FriendlyByteBuf buf) {
        return new PacketSetLauncherHat(buf.readUtf(), buf.readEnum(InteractionHand.class));
    }

    public static void handle(PacketSetLauncherHat msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ItemStack stack = player.getItemInHand(msg.hand);
            if (stack.getItem() instanceof HatLauncherItem) {
                HatLaunch.storeId(stack, msg.hatId);
                player.getInventory().setChanged();
            }
        });
        context.setPacketHandled(true);
    }
}
