package com.astryxion.hats.client.gui;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.client.hat.ClientHatUnlocks;
import com.astryxion.hats.client.gui.trade.TradeHatPreview;
import com.astryxion.hats.common.launcher.HatLaunch;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketSetLauncherHat;
import com.astryxion.hats.common.trade.TradeHats;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class HatLaunchSelectScreen extends Screen {

    private static final int PANEL_W = 260;
    private static final int PANEL_H = 180;
    private static final int CELL = 48;

    private final InteractionHand hand;
    private final List<Identifier> hats = new ArrayList<>();
    private int panelX;
    private int panelY;
    private int scroll;

    public HatLaunchSelectScreen(InteractionHand hand) {
        super(Component.translatable("hats.launcher.select"));
        this.hand = hand;
    }

    public static void open(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new HatLaunchSelectScreen(hand));
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
        hats.clear();
        hats.add(null);
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            for (Item item : HatsGui.unlockedHats(player)) {
                var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
                if (id != null) {
                    hats.add(id);
                }
            }
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(panelX + PANEL_W / 2 - 40, panelY + PANEL_H - 22, 80, 18).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + PANEL_H + 1, 0xFF555555);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xFFC6C6C6);
        graphics.text(font, title, panelX + 8, panelY + 6, 0xFF222222, false);
        Player player = Minecraft.getInstance().player;
        int cols = Math.max(1, (PANEL_W - 16) / CELL);
        int gridY = panelY + 22;
        int gridH = PANEL_H - 50;
        graphics.enableScissor(panelX + 6, gridY, panelX + PANEL_W - 6, gridY + gridH);
        for (int i = 0; i < hats.size(); i++) {
            int row = i / cols;
            int col = i % cols;
            int x = panelX + 8 + col * CELL;
            int y = gridY + 2 + row * CELL - scroll;
            if (y + CELL < gridY || y > gridY + gridH) {
                continue;
            }
            graphics.fill(x, y, x + CELL - 4, y + CELL - 4, 0xFF404040);
            Identifier id = hats.get(i);
            if (id == null) {
                graphics.centeredText(font, "?", x + (CELL - 4) / 2, y + 16, 0xFFFFFFFF);
            } else if (player != null) {
                Item item = TradeHats.item(id);
                if (item != null) {
                    TradeHatPreview.render(graphics, player, item, x, y, CELL - 4, CELL - 16);
                }
                int count = ClientHatUnlocks.count(id);
                if (count > 1) {
                    graphics.text(font, "x" + count, x + 4, y + CELL - 16, 0xFFFFFFFF, false);
                }
            }
        }
        graphics.disableScissor();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int cols = Math.max(1, (PANEL_W - 16) / CELL);
        int gridY = panelY + 22;
        int gridH = PANEL_H - 50;
        if (mouseY >= gridY && mouseY < gridY + gridH) {
            int col = (int) ((mouseX - panelX - 8) / CELL);
            int row = (int) ((mouseY - gridY - 2 + scroll) / CELL);
            int index = row * cols + col;
            if (col >= 0 && col < cols && index >= 0 && index < hats.size()) {
                Identifier id = hats.get(index);
                HatPacketHandler.sendToServer(new PacketSetLauncherHat(
                        id == null ? HatLaunch.RANDOM_ID : id.toString(), hand));
                onClose();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int cols = Math.max(1, (PANEL_W - 16) / CELL);
        int rows = (int) Math.ceil(hats.size() / (double) cols);
        int max = Math.max(0, rows * CELL - (PANEL_H - 50));
        scroll = Math.max(0, Math.min(max, scroll - (int) (scrollY * 12)));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
