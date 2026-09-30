package com.astryxion.hats.client.gui.trade;

import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.trade.ClientTradeFlow;
import com.astryxion.hats.client.trade.ClientTradeState;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.trade.PacketNearbyPlayersList;
import com.astryxion.hats.common.network.trade.PacketRequestNearbyPlayers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TradePartnerSelectScreen extends Screen {

    private static final int PANEL_W = 220;
    private static final int PANEL_H = 180;
    private static final int ROW_H = 16;
    private static final int LIST_VISIBLE_ROWS = 7;
    private static final long AUTO_REFRESH_INTERVAL_MS = 1500L;

    private int panelX;
    private int panelY;
    private int listX;
    private int listY;
    private int listW;
    private int listH;
    private int scroll;
    private long lastRefreshMs;

    public TradePartnerSelectScreen() {
        super(Component.translatable("hats.trade.choosePartner"));
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
        addRenderableWidget(Button.builder(Component.translatable("hats.trade.refresh"), button -> requestRefresh())
                .bounds(panelX + PANEL_W - 64, panelY + PANEL_H - 24, 60, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> HatsGui.open())
                .bounds(panelX + 4, panelY + PANEL_H - 24, 60, 20).build());
        listX = panelX + 8;
        listY = panelY + 28;
        listW = PANEL_W - 16;
        listH = LIST_VISIBLE_ROWS * ROW_H;
        if (System.currentTimeMillis() - lastRefreshMs > 250L) {
            requestRefresh();
        }
    }

    private void requestRefresh() {
        ClientTradeState.clearNearby();
        HatPacketHandler.CHANNEL.sendToServer(new PacketRequestNearbyPlayers());
        lastRefreshMs = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() - lastRefreshMs >= AUTO_REFRESH_INTERVAL_MS) {
            HatPacketHandler.CHANNEL.sendToServer(new PacketRequestNearbyPlayers());
            lastRefreshMs = System.currentTimeMillis();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + PANEL_H + 1, 0xFF555555);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xFFC6C6C6);
        graphics.fill(panelX + 1, panelY + 1, panelX + PANEL_W - 1, panelY + 2, 0xFFFFFFFF);
        graphics.fill(panelX + 1, panelY + PANEL_H - 2, panelX + PANEL_W - 1, panelY + PANEL_H - 1, 0xFF555555);
        graphics.drawString(font, title, panelX + (PANEL_W - font.width(title)) / 2, panelY + 8, 0xFF333333, false);

        if (ClientTradeState.hasIncomingOffers()) {
            int headerY = listY - 14;
            int count = ClientTradeState.getIncomingOffers().size();
            String text = Component.translatable("hats.trade.incomingClick", count).getString();
            boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= headerY && mouseY < headerY + 12;
            graphics.fill(listX, headerY, listX + listW, headerY + 12, hovered ? 0xFFFFC04D : 0xFFFFAA00);
            graphics.drawString(font, text, listX + (listW - font.width(text)) / 2, headerY + 2, 0xFF222222, false);
        }

        if (!ClientTradeState.isNearbyReceived()) {
            int dots = (int) ((System.currentTimeMillis() / 400) % 4);
            String loading = Component.translatable("hats.trade.fetching").getString() + ".".repeat(dots);
            graphics.drawString(font, loading, panelX + (PANEL_W - font.width(loading)) / 2, panelY + PANEL_H / 2 - 4, 0xFF333333, false);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        List<PacketNearbyPlayersList.Entry> nearby = ClientTradeState.getNearby();
        graphics.fill(listX - 1, listY - 1, listX + listW + 1, listY + listH + 1, 0xFF555555);
        graphics.fill(listX, listY, listX + listW, listY + listH, 0xFF2B2B2B);
        if (nearby.isEmpty()) {
            String empty = Component.translatable("hats.trade.noPlayers").getString();
            graphics.drawString(font, empty, panelX + (PANEL_W - font.width(empty)) / 2, listY + listH / 2 - 4, 0xFFAAAAAA, false);
        } else {
            int visible = Math.min(LIST_VISIBLE_ROWS, nearby.size() - scroll);
            for (int i = 0; i < visible; i++) {
                int rowY = listY + i * ROW_H;
                boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + ROW_H;
                if (hovered) {
                    graphics.fill(listX, rowY, listX + listW, rowY + ROW_H, 0xFF3D5A80);
                }
                graphics.drawString(font, nearby.get(scroll + i).name(), listX + 6, rowY + 4, 0xFFFFFFFF, false);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && ClientTradeState.hasIncomingOffers()) {
            int headerY = listY - 14;
            if (mouseX >= listX && mouseX < listX + listW && mouseY >= headerY && mouseY < headerY + 12) {
                ClientTradeState.IncomingOffer first = ClientTradeState.getIncomingOffers().iterator().next();
                ClientTradeFlow.openIncoming(first.offerId);
                return true;
            }
        }
        if (button == 0 && ClientTradeState.isNearbyReceived()) {
            List<PacketNearbyPlayersList.Entry> nearby = ClientTradeState.getNearby();
            int visible = Math.min(LIST_VISIBLE_ROWS, nearby.size() - scroll);
            for (int i = 0; i < visible; i++) {
                int rowY = listY + i * ROW_H;
                if (mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + ROW_H) {
                    PacketNearbyPlayersList.Entry entry = nearby.get(scroll + i);
                    ClientTradeFlow.openOfferBuilder(entry.uuid(), entry.name());
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            int max = Math.max(0, ClientTradeState.getNearby().size() - LIST_VISIBLE_ROWS);
            scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(delta)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
