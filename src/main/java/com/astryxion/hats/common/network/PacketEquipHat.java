package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.server.ServerHatHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketEquipHat {

    private final String hatId;

    public PacketEquipHat(String hatId) {
        this.hatId = hatId;
    }

    public static void encode(PacketEquipHat msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.hatId);
    }

    public static PacketEquipHat decode(FriendlyByteBuf buf) {
        return new PacketEquipHat(buf.readUtf());
    }

    public static void handle(PacketEquipHat msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // 1. Actually equip the hat (This still happens in Creative so you can see the hat!)
                ServerHatHandler.handleEquip(player, msg.hatId);

                // 2. 🔧 Trigger Universal "Trendsetter" Advancement
                // 🛑 CREATIVE CHECK: Only award if the player is NOT in Creative Mode
                if (!player.isCreative()) {
                    Advancement adv = player.getServer().getAdvancements()
                            .getAdvancement(new ResourceLocation(Hats.MODID, "hats/trendsetter"));

                    if (adv != null) {
                        player.getAdvancements().award(adv, "unlock_via_code");
                    }
                }
            }
        });

        ctx.get().setPacketHandled(true);
    }
}