package com.astryxion.hats.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncHat {

    private final CompoundTag data;

    public PacketSyncHat(CompoundTag tag) {
        this.data = tag;
    }

    public static void encode(PacketSyncHat msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static PacketSyncHat decode(FriendlyByteBuf buf) {
        return new PacketSyncHat(buf.readNbt());
    }

    public static void handle(PacketSyncHat msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (net.minecraftforge.fml.loading.FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            var player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null && msg.data != null) {
                com.astryxion.hats.common.capability.HatDataAccess.apply(player, msg.data);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}