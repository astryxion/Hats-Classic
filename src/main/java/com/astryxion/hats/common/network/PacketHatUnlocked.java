package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client packet (payload only). Client-side handling lives in
 * {@link com.astryxion.hats.client.network.HatClientPacketHandlers} so dedicated servers never load GUI classes.
 */
public class PacketHatUnlocked implements CustomPacketPayload {

    private static final String CLIENT_HAT_HANDLERS = "com.astryxion.hats.client.network.HatClientPacketHandlers";

    public static final CustomPacketPayload.Type<PacketHatUnlocked> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "hat_unlocked"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketHatUnlocked> STREAM_CODEC =
            StreamCodec.of(PacketHatUnlocked::encode, PacketHatUnlocked::decode);

    private final ItemStack hatStack;

    public PacketHatUnlocked(ItemStack hatStack) {
        this.hatStack = hatStack.copy();
    }

    public ItemStack getHatStack() {
        return hatStack;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketHatUnlocked msg) {
        ItemStack.STREAM_CODEC.encode(buf, msg.hatStack);
    }

    public static PacketHatUnlocked decode(RegistryFriendlyByteBuf buf) {
        return new PacketHatUnlocked(ItemStack.STREAM_CODEC.decode(buf));
    }

    /**
     * Invoked only on the physical client when the S2C packet is received; uses reflection so this class has no
     * compile-time dependency on client types (dedicated server must not load Toast / Minecraft.gui).
     */
    public static void handle(PacketHatUnlocked msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() != Dist.CLIENT) {
                return;
            }
            try {
                Class.forName(CLIENT_HAT_HANDLERS)
                        .getMethod("handleHatUnlocked", PacketHatUnlocked.class)
                        .invoke(null, msg);
            } catch (Throwable t) {
                Hats.LOGGER.error("Failed to handle hat unlocked packet on client", t);
            }
        });
    }
}
