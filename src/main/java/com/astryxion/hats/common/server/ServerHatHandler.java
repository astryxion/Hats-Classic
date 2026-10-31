package com.astryxion.hats.common.server;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.HatAdvancements;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.PlayerHatData;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketSyncHat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


/**
 * Server-side hat equip + sync handler
 */
public final class ServerHatHandler {

    private ServerHatHandler() {}

    // =========================
    // EQUIP HANDLER (FROM PACKET)
    // =========================

    public static void handleEquip(ServerPlayer player, String hatId) {
        Identifier id = Identifier.tryParse(hatId);
        Item item = id != null ? BuiltInRegistries.ITEM.getValue(id) : null;
        handleEquip(player, item == null || "none".equals(hatId) ? ItemStack.EMPTY : new ItemStack(item));
    }

    public static void handleEquip(ServerPlayer player, ItemStack requestedHat) {

        if (player == null)
            return;

        // =====================
        // UNEQUIP
        // =====================

        if (requestedHat == null || requestedHat.isEmpty()) {

            HatManager.clearHat(player);
            HatPacketHandler.syncPlayerHatPartToTracking(player);

            // 🔧 FIXED: Tell the capability memory we are wearing nothing
            PlayerHatData data = player.getData(HatDataCapability.HAT_DATA);
            data.setEquippedHat(null);

            HatPacketHandler.sendToPlayer(
                    player,
                    new PacketSyncHat(data.serializeNBT())
            );

            return;
        }

        // =====================
        // EQUIP
        // =====================

        Item item = requestedHat.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null || !Hats.MODID.equals(id.getNamespace()))
            return;

        // Equip on server (Visual/Part layer)
        ItemStack stack = requestedHat.copy();
        stack.setCount(1);
        HatManager.setHatStack(player, stack);
        HatPacketHandler.syncPlayerHatPartToTracking(player);
        HatAdvancements.onEquip(player, stack);

        // 🔧 FIXED: Tell the capability memory exactly which hat we are wearing
        PlayerHatData data = player.getData(HatDataCapability.HAT_DATA);
        data.setEquippedHat(id);

        HatPacketHandler.sendToPlayer(
                player,
                new PacketSyncHat(data.serializeNBT())
        );
    }
}