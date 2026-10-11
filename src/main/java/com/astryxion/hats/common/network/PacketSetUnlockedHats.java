package com.astryxion.hats.common.network;

import com.astryxion.hats.common.data.ServerHatUnlocks;
import com.astryxion.hats.common.trade.TradeHats;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Authoritative wardrobe sync, including stack counts. Trades also carry the
 * gained/lost ids so the proposer applies the same local swap the accepter
 * already does on click.
 */
public class PacketSetUnlockedHats implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSetUnlockedHats> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("hats", "set_unlocked_hats"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSetUnlockedHats> STREAM_CODEC =
            StreamCodec.of(PacketSetUnlockedHats::encode, PacketSetUnlockedHats::decode);

    private final List<Identifier> unlocked;
    private final List<Integer> counts;
    private final List<Identifier> gained;
    private final List<Identifier> lost;
    @Nullable
    private final Identifier equipped;
    private final boolean openMenu;

    public PacketSetUnlockedHats(Collection<Identifier> unlocked, @Nullable Identifier equipped) {
        this(unlocked, equipped, false, List.of(), List.of(), List.of());
    }

    public PacketSetUnlockedHats(
            Collection<Identifier> unlocked,
            @Nullable Identifier equipped,
            boolean openMenu,
            Collection<Identifier> gained,
            Collection<Identifier> lost
    ) {
        this(unlocked, equipped, openMenu, gained, lost, List.of());
    }

    public PacketSetUnlockedHats(
            Collection<Identifier> unlocked,
            @Nullable Identifier equipped,
            boolean openMenu,
            Collection<Identifier> gained,
            Collection<Identifier> lost,
            List<Integer> counts
    ) {
        this.unlocked = List.copyOf(unlocked);
        this.equipped = equipped;
        this.openMenu = openMenu;
        this.gained = List.copyOf(gained);
        this.lost = List.copyOf(lost);
        this.counts = List.copyOf(counts);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PacketSetUnlockedHats msg) {
        writeIds(buf, msg.unlocked);
        buf.writeBoolean(msg.equipped != null);
        if (msg.equipped != null) {
            buf.writeIdentifier(msg.equipped);
        }
        buf.writeBoolean(msg.openMenu);
        writeIds(buf, msg.gained);
        writeIds(buf, msg.lost);
        buf.writeVarInt(msg.counts.size());
        for (int count : msg.counts) {
            buf.writeVarInt(count);
        }
    }

    public static PacketSetUnlockedHats decode(RegistryFriendlyByteBuf buf) {
        List<Identifier> unlocked = readIds(buf);
        Identifier equipped = buf.readBoolean() ? buf.readIdentifier() : null;
        boolean openMenu = buf.readBoolean();
        List<Identifier> gained = readIds(buf);
        List<Identifier> lost = readIds(buf);
        int countSize = buf.readVarInt();
        List<Integer> counts = new ArrayList<>(countSize);
        for (int i = 0; i < countSize; i++) {
            counts.add(buf.readVarInt());
        }
        return new PacketSetUnlockedHats(unlocked, equipped, openMenu, gained, lost, counts);
    }

    public static void handle(PacketSetUnlockedHats msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist() != Dist.CLIENT) {
                return;
            }
            applyClient(msg.unlocked, msg.equipped, msg.openMenu, msg.gained, msg.lost, msg.counts);
        });
    }

    public static void send(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ServerHatUnlocks store = ServerHatUnlocks.get(player);
        LinkedHashMap<Identifier, Integer> owned = store.copyCounts(player);
        send(player, owned.keySet(), TradeHats.equippedId(player), false, List.of(), List.of(),
                new ArrayList<>(owned.values()));
    }

    public static void send(ServerPlayer player, Collection<Identifier> unlocked, @Nullable Identifier equipped) {
        send(player, unlocked, equipped, false, List.of(), List.of(), countsFor(player, unlocked));
    }

    public static void sendTrade(
            ServerPlayer player,
            Collection<Identifier> unlocked,
            @Nullable Identifier equipped,
            Collection<Identifier> gained,
            Collection<Identifier> lost
    ) {
        send(player, unlocked, equipped, true, gained, lost, countsFor(player, unlocked));
    }

    private static List<Integer> countsFor(ServerPlayer player, Collection<Identifier> unlocked) {
        return ServerHatUnlocks.get(player).countsOf(unlocked, player);
    }

    private static void send(
            ServerPlayer player,
            Collection<Identifier> unlocked,
            @Nullable Identifier equipped,
            boolean openMenu,
            Collection<Identifier> gained,
            Collection<Identifier> lost,
            List<Integer> counts
    ) {
        if (player == null) {
            return;
        }
        List<Identifier> ids = List.copyOf(unlocked);
        List<Identifier> gainedIds = List.copyOf(gained);
        List<Identifier> lostIds = List.copyOf(lost);
        List<Integer> amounts = List.copyOf(counts);
        HatPacketHandler.sendToPlayer(player, new PacketSetUnlockedHats(ids, equipped, openMenu, gainedIds, lostIds, amounts));
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            java.util.UUID target = player.getUUID();
            String targetName = player.getGameProfile().name();
            minecraft.execute(() -> {
                if (minecraft.player == null) {
                    return;
                }
                if (!minecraft.player.getUUID().equals(target)
                        && !minecraft.player.getGameProfile().name().equals(targetName)) {
                    return;
                }
                applyClient(ids, equipped, openMenu, gainedIds, lostIds, amounts);
            });
        }
    }

    private static void applyClient(
            List<Identifier> unlocked,
            @Nullable Identifier equipped,
            boolean openMenu,
            List<Identifier> gained,
            List<Identifier> lost,
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

    private static void writeIds(FriendlyByteBuf buf, List<Identifier> ids) {
        buf.writeVarInt(ids.size());
        for (Identifier id : ids) {
            buf.writeIdentifier(id);
        }
    }

    private static List<Identifier> readIds(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Identifier> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(buf.readIdentifier());
        }
        return ids;
    }
}
