package com.astryxion.hats.client.gui;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.Config;
import com.astryxion.hats.Hats;
import com.astryxion.hats.client.hat.ClientHatUnlocks;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.GuiStyle;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketOpenCosmeticMenu;
import com.astryxion.hats.common.network.PacketOpenHuntingMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HatsGui {

    private HatsGui() {}

    public static Screen create() {
        return Config.guiStyle == GuiStyle.MODERN ? new ModernHatsScreen() : new HatScreen();
    }

    /** Tells the server the player wardrobe opened, once per screen. Hat stands do not count. */
    public static void notifyMenuOpened(boolean hatStand) {
        if (hatStand) {
            return;
        }
        if (Config.hatMode == HatMode.COSMETIC) {
            HatPacketHandler.sendToServer(new PacketOpenCosmeticMenu());
        } else {
            HatPacketHandler.sendToServer(new PacketOpenHuntingMenu());
        }
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.setScreen(create());
    }

    /** Rebuild the hat menu after inventory sync so a just-accepted trade is visible. */
    public static void refreshIfOpen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (isHatMenu(minecraft.screen)) {
            open();
        }
    }

    public static boolean isHatMenu(Screen screen) {
        return screen instanceof HatScreen || screen instanceof ModernHatsScreen;
    }

    public static LivingEntity getPreviewEntity() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof HatScreen legacy) {
            return legacy.previewTarget();
        }
        if (minecraft.screen instanceof ModernHatsScreen modern) {
            return modern.previewTarget();
        }
        return null;
    }

    public static List<Item> unlockedHats(Player player) {
        List<Item> hats = new ArrayList<>();
        if (player == null) {
            return hats;
        }

        boolean showAll = player.isCreative() || Config.hatMode == HatMode.COSMETIC;
        for (Item item : BuiltInRegistries.ITEM.stream().toList()) {
            if (!(item instanceof HatItem)) {
                continue;
            }
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !id.getNamespace().equals(Hats.MODID)) {
                continue;
            }
            if (id.getPath().toLowerCase(Locale.ROOT).contains("icon")) {
                continue;
            }
            boolean owned = ClientHatUnlocks.hasOverride()
                    ? ClientHatUnlocks.has(id)
                    : player.getData(HatDataCapability.HAT_DATA).hasHat(id);
            if (showAll || owned) {
                hats.add(item);
            }
        }
        return hats;
    }
}
