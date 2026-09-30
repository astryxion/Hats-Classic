package com.astryxion.hats.common.network;

import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hatstand.HatStandEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketUpdateHatStand(int entityId, ItemStack hat) {

    public static void encode(PacketUpdateHatStand message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeItem(message.hat);
    }

    public static PacketUpdateHatStand decode(FriendlyByteBuf buffer) {
        return new PacketUpdateHatStand(buffer.readVarInt(), buffer.readItem());
    }

    public static void handle(PacketUpdateHatStand message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            Entity entity = player.level().getEntity(message.entityId);
            if (!(entity instanceof HatStandEntity stand) || player.distanceToSqr(stand) > 64.0D) {
                return;
            }
            ItemStack requested = message.hat.copy();
            if (!requested.isEmpty() && !(requested.getItem() instanceof HatItem)) {
                return;
            }
            requested.setCount(Math.min(1, requested.getCount()));
            if (requested.isEmpty()) {
                HatManager.clearHat(stand);
            } else {
                HatManager.setHatStack(stand, requested);
            }
        });
        context.setPacketHandled(true);
    }
}
