package com.astryxion.hats.common.network;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.server.ServerHatHandler;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

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

    public static void handle(PacketEquipHat msg, ServerPlayer player) {
        if (player != null) {
            ServerHatHandler.handleEquip(player, msg.hatId);
            if (!player.isCreative()) {
                AdvancementHolder holder = player.getServer().getAdvancements()
                        .get(ResourceLocation.fromNamespaceAndPath(Hats.MODID, "hats/trendsetter"));
                if (holder != null) {
                    player.getAdvancements().award(holder, "unlock_via_code");
                }
            }
        }
    }
}
