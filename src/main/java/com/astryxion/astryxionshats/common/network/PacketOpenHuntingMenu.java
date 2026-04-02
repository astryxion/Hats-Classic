package com.astryxion.astryxionshats.common.network;

import com.astryxion.astryxionshats.AstryxionsHats;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketOpenHuntingMenu {
    public PacketOpenHuntingMenu() {}
    public PacketOpenHuntingMenu(FriendlyByteBuf buffer) {}
    public void toBytes(FriendlyByteBuf buffer) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();

            // 🛑 Check if the player exists AND is NOT in Creative Mode
            if (player != null && !player.isCreative()) {
                Advancement advancement = player.getServer().getAdvancements()
                        .getAdvancement(new ResourceLocation(AstryxionsHats.MODID, "hats/hunt_begin"));

                if (advancement != null) {
                    AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                    if (!progress.isDone()) {
                        for (String criterion : progress.getRemainingCriteria()) {
                            player.getAdvancements().award(advancement, criterion);
                        }
                    }
                }
            }
        });
        return true;
    }
}