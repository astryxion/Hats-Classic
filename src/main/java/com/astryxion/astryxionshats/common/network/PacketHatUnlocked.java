package com.astryxion.astryxionshats.common.network;

import com.astryxion.astryxionshats.client.gui.toast.HatToast;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → Client packet
 * Shows the hat-unlocked popup with the EXACT hat item.
 */
public class PacketHatUnlocked {

    private final ItemStack hatStack;

    /**
     * IMPORTANT:
     * Always store a COPY so the stack can't be mutated later.
     */
    public PacketHatUnlocked(ItemStack hatStack) {
        this.hatStack = hatStack.copy();
    }

    // =====================
    // Encode
    // =====================

    public static void encode(PacketHatUnlocked msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.hatStack);
    }

    // =====================
    // Decode
    // =====================

    public static PacketHatUnlocked decode(FriendlyByteBuf buf) {
        return new PacketHatUnlocked(buf.readItem());
    }

    // =====================
    // Handle (CLIENT ONLY)
    // =====================

    public static void handle(
            PacketHatUnlocked msg,
            Supplier<NetworkEvent.Context> ctx
    ) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // 🔔 Always show the correct hat
            mc.getToasts().addToast(new HatToast(msg.hatStack));
        });

        ctx.get().setPacketHandled(true);
    }
}
