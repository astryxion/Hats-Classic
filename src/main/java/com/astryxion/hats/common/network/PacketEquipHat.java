package com.astryxion.hats.common.network;

import com.astryxion.hats.common.server.ServerHatHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class PacketEquipHat {

    private final ItemStack hat;

    public PacketEquipHat(String hatId) {
        ResourceLocation id = ResourceLocation.tryParse(hatId);
        Item item = id != null ? ForgeRegistries.ITEMS.getValue(id) : null;
        this.hat = item == null || "none".equals(hatId) ? ItemStack.EMPTY : new ItemStack(item);
    }

    public PacketEquipHat(ItemStack hat) {
        this.hat = hat == null ? ItemStack.EMPTY : hat.copy();
    }

    public static void encode(PacketEquipHat msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.hat);
    }

    public static PacketEquipHat decode(FriendlyByteBuf buf) {
        return new PacketEquipHat(buf.readItem());
    }

    public static void handle(PacketEquipHat msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // 1. Actually equip the hat (This still happens in Creative so you can see the hat!)
                ServerHatHandler.handleEquip(player, msg.hat);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}