package com.astryxion.hats.client.gui.trade;

import com.astryxion.hats.client.gui.HatNameScroller;
import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.trade.ClientTradeState;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.trade.PacketRespondTradeOffer;
import com.astryxion.hats.common.trade.TradeHats;
import com.astryxion.hats.common.trade.TradeOffer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.List;

public class TradeIncomingScreen extends Screen {

    private static final int PANEL_W = 300;
    private static final int PANEL_H = 210;
    private static final int CELL_W = 44;
    private static final int CELL_H = 50;
    private static final int NAME_H = 10;
    private static final int ROW_H = CELL_H + NAME_H;

    private final ClientTradeState.IncomingOffer offer;
    private int panelX;
    private int panelY;
    private int colY;
    private int colH;
    private int leftScroll;
    private int rightScroll;

    public TradeIncomingScreen(ClientTradeState.IncomingOffer offer) {
        super(Component.translatable("hats.trade.offerFrom", offer.senderName));
        this.offer = offer;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
        addRenderableWidget(Button.builder(Component.translatable("hats.trade.accept").withStyle(ChatFormatting.GREEN),
                button -> respond(true)).bounds(panelX + PANEL_W / 2 - 76, panelY + PANEL_H - 22, 70, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("hats.trade.decline").withStyle(ChatFormatting.RED),
                button -> respond(false)).bounds(panelX + PANEL_W / 2 + 6, panelY + PANEL_H - 22, 70, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> HatsGui.open())
                .bounds(panelX + 6, panelY + PANEL_H - 22, 50, 18).build());
        colY = panelY + 36;
        colH = (panelY + PANEL_H - 40) - colY;
    }

    private void respond(boolean accept) {
        HatPacketHandler.CHANNEL.sendToServer(new PacketRespondTradeOffer(offer.offerId, accept));
        ClientTradeState.removeIncomingOffer(offer.offerId);
        if (accept) {
            net.minecraft.client.Minecraft minecraft = Minecraft.getInstance();
            com.astryxion.hats.client.hat.ClientHatUnlocks.ensureSeeded(minecraft.player);
            com.astryxion.hats.client.hat.ClientHatUnlocks.applySwap(offer.senderHats, offer.receiverHats);
        }
        HatsGui.open();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + PANEL_H + 1, 0xFF555555);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xFFC6C6C6);
        graphics.fill(panelX + 1, panelY + 1, panelX + PANEL_W - 1, panelY + 2, 0xFFFFFFFF);

        Component header = Component.translatable("hats.trade.offerFrom", offer.senderName)
                .withStyle(ChatFormatting.DARK_GRAY);
        graphics.drawString(font, header, panelX + (PANEL_W - font.width(header)) / 2, panelY + 6, 0xFF222222, false);

        long remaining = Math.max(0L, TradeOffer.EXPIRY_MS - (System.currentTimeMillis() - offer.createdAtClientMs));
        long seconds = remaining / 1000L;
        String expires = Component.translatable("hats.trade.expires", seconds / 60, String.format("%02d", seconds % 60)).getString();
        graphics.drawString(font, expires, panelX + (PANEL_W - font.width(expires)) / 2, panelY + 18, 0xFF555555, false);

        int colW = (PANEL_W - 18) / 2;
        Player player = Minecraft.getInstance().player;
        renderColumn(graphics, panelX + 6, colY, colW, Component.translatable("hats.trade.theyGive").getString(),
                offer.senderHats, leftScroll, player);
        renderColumn(graphics, panelX + 12 + colW, colY, colW, Component.translatable("hats.trade.youGive").getString(),
                offer.receiverHats, rightScroll, player);

        if (offer.receiverHats.isEmpty()) {
            String note = Component.translatable("hats.trade.giftNote").getString();
            graphics.drawString(font, note, panelX + (PANEL_W - font.width(note)) / 2, panelY + PANEL_H - 36, 0xFF227722, false);
        } else if (offer.senderHats.isEmpty()) {
            String note = Component.translatable("hats.trade.wantGiftNote").getString();
            graphics.drawString(font, note, panelX + (PANEL_W - font.width(note)) / 2, panelY + PANEL_H - 36, 0xFF884400, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderColumn(
            GuiGraphics graphics,
            int columnX,
            int columnY,
            int columnW,
            String header,
            List<ResourceLocation> hats,
            int scroll,
            Player player
    ) {
        graphics.drawString(font, header, columnX, columnY, 0xFF222222, false);
        int rowsY = columnY + 11;
        int rowsH = colH - 11;
        graphics.fill(columnX - 1, rowsY - 1, columnX + columnW + 1, rowsY + rowsH + 1, 0xFF555555);
        graphics.fill(columnX, rowsY, columnX + columnW, rowsY + rowsH, 0xFF2B2B2B);
        if (hats.isEmpty()) {
            String empty = Component.translatable("hats.trade.nothing").getString();
            graphics.drawString(font, empty, columnX + (columnW - font.width(empty)) / 2, rowsY + rowsH / 2 - 4, 0xFF777777, false);
            return;
        }
        int cellsPerRow = Math.max(1, (columnW - 6) / CELL_W);
        int gap = (columnW - 6 - cellsPerRow * CELL_W) / Math.max(1, cellsPerRow + 1);
        graphics.enableScissor(columnX + 1, rowsY + 1, columnX + columnW - 1, rowsY + rowsH - 1);
        for (int i = 0; i < hats.size(); i++) {
            int row = i / cellsPerRow;
            int col = i % cellsPerRow;
            int hatX = columnX + 3 + gap + col * (CELL_W + gap);
            int hatY = rowsY + 3 + row * ROW_H - scroll;
            if (hatY + ROW_H <= rowsY || hatY >= rowsY + rowsH) {
                continue;
            }
            renderHatCell(graphics, hats.get(i), hatX, hatY, player);
        }
        graphics.disableScissor();
    }

    private void renderHatCell(GuiGraphics graphics, ResourceLocation id, int cellX, int cellY, Player player) {
        graphics.fill(cellX, cellY, cellX + CELL_W, cellY + CELL_H, 0xFF404040);
        graphics.fill(cellX, cellY, cellX + CELL_W, cellY + 1, 0xFF555555);
        graphics.fill(cellX, cellY, cellX + 1, cellY + CELL_H, 0xFF555555);
        graphics.fill(cellX, cellY + CELL_H - 1, cellX + CELL_W, cellY + CELL_H, 0xFF2A2A2A);
        graphics.fill(cellX + CELL_W - 1, cellY, cellX + CELL_W, cellY + CELL_H, 0xFF2A2A2A);
        Item item = TradeHats.item(id);
        if (item != null && player != null) {
            TradeHatPreview.render(graphics, player, item, cellX, cellY, CELL_W, CELL_H);
        }
        String name = item == null ? id.getPath() : item.getDescription().getString();
        HatNameScroller.draw(graphics, font, name, cellX, cellY + CELL_H + 1, CELL_W, 0xFFDDDDDD);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int colW = (PANEL_W - 18) / 2;
        int leftX = panelX + 6;
        int rightX = panelX + 12 + colW;
        int rowsY = colY + 11;
        int rowsH = colH - 11;
        if (mouseY >= rowsY && mouseY < rowsY + rowsH) {
            if (mouseX >= leftX && mouseX < leftX + colW) {
                leftScroll = clampScroll(leftScroll - (int) (delta * 12), offer.senderHats.size(), colW, rowsH);
                return true;
            }
            if (mouseX >= rightX && mouseX < rightX + colW) {
                rightScroll = clampScroll(rightScroll - (int) (delta * 12), offer.receiverHats.size(), colW, rowsH);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private int clampScroll(int proposed, int hatCount, int columnW, int rowsH) {
        int cellsPerRow = Math.max(1, (columnW - 6) / CELL_W);
        int contentH = (int) Math.ceil(hatCount / (double) cellsPerRow) * ROW_H + 6;
        return Math.max(0, Math.min(Math.max(0, contentH - rowsH), proposed));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
