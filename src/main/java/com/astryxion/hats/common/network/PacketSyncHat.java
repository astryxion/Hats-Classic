package com.astryxion.hats.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSyncHat implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncHat> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "sync_hat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncHat> STREAM_CODEC =
            StreamCodec.of(PacketSyncHat::encode, PacketSyncHat::decode);

    private final CompoundTag data;

    public PacketSyncHat(CompoundTag tag) {
        this.data = tag;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketSyncHat msg) {
        buf.writeNbt(msg.data);
    }

    public static PacketSyncHat decode(RegistryFriendlyByteBuf buf) {
        return new PacketSyncHat(buf.readNbt());
    }

    public static void handle(PacketSyncHat msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (net.neoforged.fml.loading.FMLEnvironment.getDist() != Dist.CLIENT) {
                return;
            }
            var player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null && msg.data != null) {
                com.astryxion.hats.common.capability.HatDataAccess.apply(player, msg.data);
            }
        });
    }
}
