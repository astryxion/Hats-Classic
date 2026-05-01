package com.astryxion.hats.common.network;

import com.astryxion.hats.common.hat.HatPartCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

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

    public static void handle(PacketSyncHatPart msg) {
        if (Minecraft.getInstance().level != null) {
            Entity entity = Minecraft.getInstance().level.getEntity(msg.playerId);
            if (entity instanceof Player player) {
                var part = HatPartCapability.getOrCreate(player);
                if (part != null) part.deserializeNBT(msg.tag);
            }
        }
    }
}
