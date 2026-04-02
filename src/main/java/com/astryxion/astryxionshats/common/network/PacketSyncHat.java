package com.astryxion.astryxionshats.common.network;

import com.astryxion.astryxionshats.common.capability.HatDataCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
            // Safety check: Only run this on the Client side
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var player = net.minecraft.client.Minecraft.getInstance().player;
                if (player != null) {
                    // TARGET THE COLLECTION DATA, NOT THE PART DATA
                    player.getCapability(HatDataCapability.HAT_DATA).ifPresent(cap -> {
                        cap.deserializeNBT(msg.data);
                    });
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}