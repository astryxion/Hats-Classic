package com.astryxion.hats.common.network;

import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hatstand.HatStandEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpdateHatStand(int entityId, ItemStack hat) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketUpdateHatStand> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "update_hat_stand"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateHatStand> STREAM_CODEC =
            StreamCodec.of(PacketUpdateHatStand::encode, PacketUpdateHatStand::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buffer, PacketUpdateHatStand message) {
        buffer.writeVarInt(message.entityId);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, message.hat);
    }

    public static PacketUpdateHatStand decode(RegistryFriendlyByteBuf buffer) {
        return new PacketUpdateHatStand(buffer.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
    }

    public static void handle(PacketUpdateHatStand message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
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
    }
}
