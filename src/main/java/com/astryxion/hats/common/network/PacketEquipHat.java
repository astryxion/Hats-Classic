package com.astryxion.hats.common.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.common.server.ServerHatHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class PacketEquipHat implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketEquipHat> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "equip_hat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketEquipHat> STREAM_CODEC =
            StreamCodec.of(PacketEquipHat::encode, PacketEquipHat::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final ItemStack hat;

    public PacketEquipHat(String hatId) {
        ResourceLocation id = ResourceLocation.tryParse(hatId);
        Item item = id != null && BuiltInRegistries.ITEM.containsKey(id) ? BuiltInRegistries.ITEM.get(id) : null;
        this.hat = item == null || "none".equals(hatId) ? ItemStack.EMPTY : new ItemStack(item);
    }

    public PacketEquipHat(ItemStack hat) {
        this.hat = hat == null ? ItemStack.EMPTY : hat.copy();
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketEquipHat msg) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, msg.hat);
    }

    public static PacketEquipHat decode(RegistryFriendlyByteBuf buf) {
        return new PacketEquipHat(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }

    public static void handle(PacketEquipHat msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player != null) {
                // 1. Actually equip the hat (This still happens in Creative so you can see the hat!)
                ServerHatHandler.handleEquip(player, msg.hat);
            }
        });

        
    }
}