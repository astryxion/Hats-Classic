package com.astryxion.hats.common.trade;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.Config;
import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.hat.PlayerHatData;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class TradeHats {

    private TradeHats() {}

    public static Identifier id(Item item) {
        return item == null ? null : BuiltInRegistries.ITEM.getKey(item);
    }

    public static Item item(Identifier id) {
        return id == null ? null : BuiltInRegistries.ITEM.getValue(id);
    }

    public static boolean isHat(Identifier id) {
        Item item = item(id);
        return item instanceof HatItem && id != null && Hats.MODID.equals(id.getNamespace())
                && !id.getPath().toLowerCase(Locale.ROOT).contains("icon");
    }

    public static Identifier equippedId(Player player) {
        return HatManager.getEquippedHatId(player);
    }

    public static boolean owns(PlayerHatData data, Identifier id) {
        return data != null && id != null && data.getUnlockedHats().contains(id);
    }

    public static boolean canOffer(PlayerHatData data, Identifier id) {
        if (!isHat(id)) {
            return false;
        }
        if (Config.hatMode == HatMode.COSMETIC) {
            return true;
        }
        return owns(data, id);
    }

    public static List<Identifier> inventory(Player player, PlayerHatData data) {
        List<Identifier> hats = new ArrayList<>();
        if (data == null) {
            return hats;
        }
        if (Config.hatMode == HatMode.COSMETIC) {
            for (Item item : BuiltInRegistries.ITEM.stream().toList()) {
                if (!(item instanceof HatItem)) {
                    continue;
                }
                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (id == null || !Hats.MODID.equals(id.getNamespace())
                        || id.getPath().toLowerCase(Locale.ROOT).contains("icon")) {
                    continue;
                }
                hats.add(id);
            }
        } else {
            for (Identifier id : data.getUnlockedHats()) {
                if (isHat(id)) {
                    hats.add(id);
                }
            }
        }
        hats.sort(Comparator.comparing(Identifier::toString));
        return hats;
    }

    public static boolean inRange(ServerPlayer a, ServerPlayer b) {
        if (a == null || b == null || a.level() != b.level()) {
            return false;
        }
        double range = TradeOfferStore.TRADE_RANGE_BLOCKS;
        return a.distanceToSqr(b) <= range * range;
    }
}
