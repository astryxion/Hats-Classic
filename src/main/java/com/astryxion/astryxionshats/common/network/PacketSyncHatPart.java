package com.astryxion.astryxionshats.common.network;

import com.astryxion.astryxionshats.common.hat.HatPartCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncHatPart {

    private final int playerId;
    private final CompoundTag tag;

    public PacketSyncHatPart(int playerId, CompoundTag tag) {
        this.playerId = playerId;
        this.tag = tag;
    }

    public static void encode(PacketSyncHatPart msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.playerId);
        buf.writeNbt(msg.tag);
    }

    public static PacketSyncHatPart decode(FriendlyByteBuf buf) {
        return new PacketSyncHatPart(buf.readInt(), buf.readNbt());
    }

    public static void handle(PacketSyncHatPart msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    var mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.level == null) return;
                    Entity entity = mc.level.getEntity(msg.playerId);
                    if (entity instanceof Player player) {
                        player.getCapability(HatPartCapability.HAT_PART).ifPresent(part ->
                                part.deserializeNBT(msg.tag));
                    }
                }));
        ctx.get().setPacketHandled(true);
    }
}