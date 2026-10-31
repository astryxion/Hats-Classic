package com.astryxion.hats.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Server → Client packet (payload only). Client-side handling lives in
 * {@link com.astryxion.hats.client.network.HatClientPacketHandlers} so dedicated servers never load GUI classes.
 */
public class PacketHatUnlocked implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketHatUnlocked> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("hats", "hat_unlocked"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketHatUnlocked> STREAM_CODEC =
            StreamCodec.of(PacketHatUnlocked::encode, PacketHatUnlocked::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private final ItemStack hatStack;

    public PacketHatUnlocked(ItemStack hatStack) {
        this.hatStack = hatStack.copy();
    }

    public ItemStack getHatStack() {
        return hatStack;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketHatUnlocked msg) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, msg.hatStack);
    }

    public static PacketHatUnlocked decode(RegistryFriendlyByteBuf buf) {
        return new PacketHatUnlocked(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }
}
