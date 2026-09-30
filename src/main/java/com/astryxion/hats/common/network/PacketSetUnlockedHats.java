package com.astryxion.hats.common.network;

import com.astryxion.hats.common.data.ServerHatUnlocks;
import com.astryxion.hats.common.trade.TradeHats;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Authoritative wardrobe sync, including stack counts. Trades also carry the
 * gained/lost ids so the proposer applies the same local swap the accepter
 * already does on click.
 */
public class PacketSetUnlockedHats {

    private final List<ResourceLocation> unlocked;
    private final List<Integer> counts;
    private final List<ResourceLocation> gained;
    private final List<ResourceLocation> lost;
    @Nullable
    private final ResourceLocation equipped;
    private final boolean openMenu;

    public PacketSetUnlockedHats(Collection<ResourceLocation> unlocked, @Nullable ResourceLocation equipped) {
        this(unlocked, equipped, false, List.of(), List.of(), List.of());
    }

    public PacketSetUnlockedHats(
            Collection<ResourceLocation> unlocked,
            @Nullable ResourceLocation equipped,
            boolean openMenu,
            Collection<ResourceLocation> gained,
            Collection<ResourceLocation> lost
    ) {
        this(unlocked, equipped, openMenu, gained, lost, List.of());
    }

    public PacketSetUnlockedHats(
            Collection<ResourceLocation> unlocked,
            @Nullable ResourceLocation equipped,
            boolean openMenu,
            Collection<ResourceLocation> gained,
            Collection<ResourceLocation> lost,
            List<Integer> counts
    ) {
        this.unlocked = List.copyOf(unlocked);
        this.equipped = equipped;
        this.openMenu = openMenu;
        this.gained = List.copyOf(gained);
        this.lost = List.copyOf(lost);
        this.counts = List.copyOf(counts);
    }

    public static void encode(PacketSetUnlockedHats msg, FriendlyByteBuf buf) {
        writeIds(buf, msg.unlocked);
        buf.writeBoolean(msg.equipped != null);
        if (msg.equipped != null) {
            buf.writeResourceLocation(msg.equipped);
        }
        buf.writeBoolean(msg.openMenu);
        writeIds(buf, msg.gained);
        writeIds(buf, msg.lost);
        buf.writeVarInt(msg.counts.size());
        for (int count : msg.counts) {
            buf.writeVarInt(count);
        }
    }

    public static PacketSetUnlockedHats decode(FriendlyByteBuf buf) {
        List<ResourceLocation> unlocked = readIds(buf);
        ResourceLocation equipped = buf.readBoolean() ? buf.readResourceLocation() : null;
        boolean openMenu = buf.readBoolean();
        List<ResourceLocation> gained = readIds(buf);
        List<ResourceLocation> lost = readIds(buf);
        int countSize = buf.readVarInt();
        List<Integer> counts = new ArrayList<>(countSize);
        for (int i = 0; i < countSize; i++) {
            counts.add(buf.readVarInt());
        }
        return new PacketSetUnlockedHats(unlocked, equipped, openMenu, gained, lost, counts);
    }

    public static void handle(PacketSetUnlockedHats msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            applyClient(msg.unlocked, msg.equipped, msg.openMenu, msg.gained, msg.lost, msg.counts);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void send(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ServerHatUnlocks store = ServerHatUnlocks.get(player);
        LinkedHashMap<ResourceLocation, Integer> owned = store.copyCounts(player);
        send(player, owned.keySet(), TradeHats.equippedId(player), false, List.of(), List.of(),
                new ArrayList<>(owned.values()));
    }

    public static void send(ServerPlayer player, Collection<ResourceLocation> unlocked, @Nullable ResourceLocation equipped) {
        send(player, unlocked, equipped, false, List.of(), List.of(), countsFor(player, unlocked));
    }

    public static void sendTrade(
            ServerPlayer player,
            Collection<ResourceLocation> unlocked,
            @Nullable ResourceLocation equipped,
            Collection<ResourceLocation> gained,
            Collection<ResourceLocation> lost
    ) {
        send(player, unlocked, equipped, true, gained, lost, countsFor(player, unlocked));
    }

    private static List<Integer> countsFor(ServerPlayer player, Collection<ResourceLocation> unlocked) {
        return ServerHatUnlocks.get(player).countsOf(unlocked, player);
    }

    private static void send(
            ServerPlayer player,
            Collection<ResourceLocation> unlocked,
            @Nullable ResourceLocation equipped,
            boolean openMenu,
            Collection<ResourceLocation> gained,
            Collection<ResourceLocation> lost,
            List<Integer> counts
    ) {
        if (player == null) {
            return;
        }
        List<ResourceLocation> ids = List.copyOf(unlocked);
        List<ResourceLocation> gainedIds = List.copyOf(gained);
        List<ResourceLocation> lostIds = List.copyOf(lost);
        List<Integer> amounts = List.copyOf(counts);
        HatPacketHandler.sendToPlayer(player, new PacketSetUnlockedHats(ids, equipped, openMenu, gainedIds, lostIds, amounts));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            java.util.UUID target = player.getUUID();
            String targetName = player.getGameProfile().getName();
            minecraft.execute(() -> {
                if (minecraft.player == null) {
                    return;
                }
                if (!minecraft.player.getUUID().equals(target)
                        && !minecraft.player.getGameProfile().getName().equals(targetName)) {
                    return;
                }
                applyClient(ids, equipped, openMenu, gainedIds, lostIds, amounts);
            });
        }
    }

    private static void applyClient(
            List<ResourceLocation> unlocked,
            @Nullable ResourceLocation equipped,
            boolean openMenu,
            List<ResourceLocation> gained,
            List<ResourceLocation> lost,
            List<Integer> counts
    ) {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        com.astryxion.hats.client.hat.ClientHatUnlocks.ensureSeeded(minecraft.player);
        if (!gained.isEmpty() || !lost.isEmpty()) {
            com.astryxion.hats.client.hat.ClientHatUnlocks.applySwap(gained, lost);
        }
        com.astryxion.hats.client.hat.ClientHatUnlocks.forceSetIfComplete(unlocked, gained, counts);
        if (minecraft.player != null) {
            com.astryxion.hats.common.capability.HatDataAccess.replaceUnlocked(
                    minecraft.player, com.astryxion.hats.client.hat.ClientHatUnlocks.all(), equipped);
        }
        if (openMenu) {
            com.astryxion.hats.client.gui.HatsGui.open();
        } else {
            com.astryxion.hats.client.gui.HatsGui.refreshIfOpen();
        }
    }

    private static void writeIds(FriendlyByteBuf buf, List<ResourceLocation> ids) {
        buf.writeVarInt(ids.size());
        for (ResourceLocation id : ids) {
            buf.writeResourceLocation(id);
        }
    }

    private static List<ResourceLocation> readIds(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<ResourceLocation> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(buf.readResourceLocation());
        }
        return ids;
    }
}
