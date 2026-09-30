package com.astryxion.hats.common.network;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.hat.HatAdvancements;
import com.astryxion.hats.common.hat.HatMode;
import net.minecraft.network.FriendlyByteBuf;
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
            if (player != null
                    && !player.isCreative()
                    && !player.isSpectator()
                    && Config.hatMode == HatMode.HUNTING) {
                HatAdvancements.award(player, "hats/hunt_begin");
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}