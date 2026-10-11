package com.astryxion.hats.client.gui.trade;

import com.astryxion.hats.client.gui.HatNameScroller;
import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.trade.ClientTradeFlow;
import com.astryxion.hats.client.trade.ClientTradeState;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.trade.PacketSendTradeOffer;
import com.astryxion.hats.common.trade.TradeErrorType;
import com.astryxion.hats.common.trade.TradeHats;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class TradeOfferScreen extends Screen {

    private static final int PANEL_W = 420;
    private static final int PANEL_H = 220;
    private static final int CELL_W = 54;
    private static final int CELL_H = 66;
    private static final int SCROLL_W = 7;
    private static final int GRID_COLS = 2;
    private static final int GRID_W = CELL_W * GRID_COLS + SCROLL_W;
    private static final int GRID_H = 128;
    private static final int SLOT_SIZE = 60;

    private final UUID targetUuid;
    private final String targetName;

    private int panelX;
    private int panelY;
    private int leftGridX;
    private int rightGridX;
    private int gridY;
    private int leftSlotX;
    private int rightSlotX;
    private int slotY;
    private int leftScroll;
    private int rightScroll;

    private final Set<Identifier> leftSelected = new LinkedHashSet<>();
    private final Set<Identifier> rightSelected = new LinkedHashSet<>();
    private String leftFilter = "";
    private String rightFilter = "";

    @Nullable
    private EditBox leftSearch;
    @Nullable
    private EditBox rightSearch;
    @Nullable
    private Button tradeButton;

    public TradeOfferScreen(UUID targetUuid, String targetName) {
        super(Component.translatable("hats.trade.with", targetName));
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
        leftGridX = panelX + 8;
        rightGridX = panelX + PANEL_W - 8 - GRID_W;
        gridY = panelY + 32;
        int center = panelX + PANEL_W / 2;
        leftSlotX = center - SLOT_SIZE - 18;
        rightSlotX = center + 18;
        slotY = gridY + (GRID_H - SLOT_SIZE) / 2;

        int searchW = GRID_W - SCROLL_W - 2;
        int searchY = gridY + GRID_H + 2;
        leftSearch = new EditBox(font, leftGridX + 1, searchY, searchW, 14, Component.translatable("hats.gui.search"));
        leftSearch.setHint(Component.translatable("hats.gui.search"));
        leftSearch.setResponder(query -> {
            leftFilter = query.toLowerCase(Locale.ROOT);
            leftScroll = 0;
        });
        addRenderableWidget(leftSearch);

        rightSearch = new EditBox(font, rightGridX + 1, searchY, searchW, 14, Component.translatable("hats.gui.search"));
        rightSearch.setHint(Component.translatable("hats.gui.search"));
        rightSearch.setResponder(query -> {
            rightFilter = query.toLowerCase(Locale.ROOT);
            rightScroll = 0;
        });
        addRenderableWidget(rightSearch);

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> back())
                .bounds(center - 80, panelY + PANEL_H - 22, 70, 18).build());
        tradeButton = addRenderableWidget(Button.builder(Component.translatable("hats.gui.trade"), button -> sendOffer())
                .bounds(center + 10, panelY + PANEL_H - 22, 70, 18).build());
        updateButtons();
    }

    private void back() {
        ClientTradeFlow.closeBuilder();
        Minecraft.getInstance().setScreen(new TradePartnerSelectScreen());
    }

    private void updateButtons() {
        if (tradeButton != null) {
            tradeButton.active = !leftSelected.isEmpty() || !rightSelected.isEmpty();
        }
    }

    private void sendOffer() {
        HatPacketHandler.sendToServer(new PacketSendTradeOffer(
                targetUuid, new ArrayList<>(leftSelected), new ArrayList<>(rightSelected)));
        ClientTradeState.setPendingOutgoing(leftSelected, rightSelected);
        ClientTradeFlow.closeBuilder();
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        TradeErrorType error = ClientTradeState.consumeError();
        if (error != null) {
            handleError(error);
            return;
        }

        graphics.fill(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + PANEL_H + 1, 0xFF555555);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xFFC6C6C6);
        graphics.fill(panelX + 1, panelY + 1, panelX + PANEL_W - 1, panelY + 2, 0xFFFFFFFF);
        graphics.fill(panelX + 1, panelY + PANEL_H - 2, panelX + PANEL_W - 1, panelY + PANEL_H - 1, 0xFF555555);
        graphics.text(font, title, panelX + (PANEL_W - font.width(title)) / 2, panelY + 8, 0xFF222222, false);

        Player player = Minecraft.getInstance().player;
        String localName = player != null ? player.getGameProfile().name() : "You";
        graphics.text(font, Component.translatable("hats.trade.you", localName), leftGridX, gridY - 12, 0xFF222222, false);
        Component them = Component.translatable("hats.trade.them", targetName);
        graphics.text(font, them, rightGridX + GRID_W - font.width(them), gridY - 12, 0xFF222222, false);

        renderGrid(graphics, mouseX, mouseY, true, player, sourceList(true), leftScroll);
        renderGrid(graphics, mouseX, mouseY, false, player, sourceList(false), rightScroll);
        renderSlot(graphics, leftSlotX, lastOf(leftSelected), leftSelected.size(), player);
        renderSlot(graphics, rightSlotX, lastOf(rightSelected), rightSelected.size(), player);
        renderArrow(graphics, !leftSelected.isEmpty(), !rightSelected.isEmpty());
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void handleError(TradeErrorType error) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.sendOverlayMessage(Component.translatable(error.messageKey()).withStyle(ChatFormatting.RED));
        }
        ClientTradeFlow.closeBuilder();
        HatsGui.open();
    }

    private List<Identifier> sourceList(boolean left) {
        List<Identifier> result = new ArrayList<>();
        String filter = left ? leftFilter : rightFilter;
        Player player = Minecraft.getInstance().player;
        if (left) {
            if (player == null) {
                return result;
            }
            java.util.Collection<Identifier> owned = com.astryxion.hats.client.hat.ClientHatUnlocks.hasOverride()
                    ? com.astryxion.hats.client.hat.ClientHatUnlocks.all()
                    : TradeHats.inventory(player, player.getData(HatDataCapability.HAT_DATA));
            for (Identifier id : owned) {
                Item item = TradeHats.item(id);
                String name = item == null ? id.getPath() : Component.translatable(item.getDescriptionId()).getString();
                if (!filter.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(filter)) {
                    continue;
                }
                result.add(id);
            }
        } else {
            List<Identifier> remote = ClientTradeState.getPeerInventory(targetUuid);
            if (remote == null) {
                return result;
            }
            for (Identifier id : remote) {
                Item item = TradeHats.item(id);
                String name = item == null ? id.getPath() : Component.translatable(item.getDescriptionId()).getString();
                if (!filter.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(filter)) {
                    continue;
                }
                result.add(id);
            }
        }
        return result;
    }

    private void renderGrid(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            boolean left,
            @Nullable Player player,
            List<Identifier> source,
            int scroll
    ) {
        int gridX = left ? leftGridX : rightGridX;
        graphics.fill(gridX, gridY, gridX + GRID_W - SCROLL_W, gridY + GRID_H, 0xFF3B3B3B);
        if (source.isEmpty()) {
            String empty = Component.translatable(left ? "hats.gui.no_hats" : "hats.trade.loading").getString();
            graphics.text(font, empty, gridX + 4, gridY + 4, 0xFFAAAAAA, false);
        } else if (player != null) {
            graphics.enableScissor(gridX + 1, gridY + 1, gridX + GRID_W - SCROLL_W - 1, gridY + GRID_H - 1);
            int row = 0;
            int col = 0;
            Set<Identifier> selected = left ? leftSelected : rightSelected;
            for (int i = 0; i < source.size(); i++) {
                int cellX = gridX + col * CELL_W;
                int cellY = gridY + row * CELL_H - scroll;
                if (cellY + CELL_H > gridY && cellY < gridY + GRID_H) {
                    Identifier id = source.get(i);
                    boolean hovered = mouseX >= cellX && mouseX < cellX + CELL_W && mouseY >= cellY && mouseY < cellY + CELL_H
                            && mouseY >= gridY && mouseY < gridY + GRID_H;
                    boolean on = selected.contains(id);
                    graphics.fill(cellX, cellY, cellX + CELL_W, cellY + CELL_H, hovered || on ? 0xFF505060 : 0xFF404040);
                    Item item = TradeHats.item(id);
                    if (item != null) {
                        TradeHatPreview.render(graphics, player, item, cellX, cellY, CELL_W, CELL_H);
                    }
                    Identifier equipped = left
                            ? TradeHats.equippedId(player)
                            : ClientTradeState.getPeerEquipped(targetUuid);
                    if (id.equals(equipped)) {
                        graphics.fill(cellX + 1, cellY + 1, cellX + CELL_W - 1, cellY + CELL_H - 1, 0x88000000);
                    }
                    if (on) {
                        graphics.fill(cellX + 1, cellY + 1, cellX + 5, cellY + CELL_H - 1, 0xFF44FF44);
                    }
                }
                if (++col >= GRID_COLS) {
                    col = 0;
                    row++;
                }
            }
            graphics.disableScissor();
        }
        renderScrollbar(graphics, gridX + GRID_W - SCROLL_W, source.size(), scroll);
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int barX, int total, int scroll) {
        graphics.fill(barX, gridY, barX + SCROLL_W, gridY + GRID_H, 0xFF2A2A2A);
        int max = maxScroll(total);
        if (max <= 0) {
            return;
        }
        int inner = GRID_H - 2;
        int thumbH = Math.max(14, inner * inner / (inner + max));
        int thumbY = gridY + 1 + (int) ((inner - thumbH) * (float) scroll / max);
        graphics.fill(barX + 1, thumbY, barX + SCROLL_W - 1, thumbY + thumbH, 0xFF777777);
    }

    private void renderSlot(GuiGraphicsExtractor graphics, int slotX, @Nullable Identifier id, int selectedCount, @Nullable Player player) {
        graphics.fill(slotX - 1, slotY - 1, slotX + SLOT_SIZE + 1, slotY + SLOT_SIZE + 1, 0xFF222222);
        graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0xFF555555);
        if (id == null || player == null) {
            return;
        }
        Item item = TradeHats.item(id);
        if (item != null) {
            TradeHatPreview.render(graphics, player, item, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            if (selectedCount > 1) {
                String tag = "x" + selectedCount;
                int tw = font.width(tag);
                graphics.fill(slotX + SLOT_SIZE - tw - 4, slotY + SLOT_SIZE - 11,
                        slotX + SLOT_SIZE - 1, slotY + SLOT_SIZE - 1, 0xCC1A4A8A);
                graphics.text(font, tag, slotX + SLOT_SIZE - tw - 2, slotY + SLOT_SIZE - 10, 0xFFFFFFFF, false);
            }
            HatNameScroller.draw(graphics, font, Component.translatable(item.getDescriptionId()).getString(),
                    slotX, slotY + SLOT_SIZE + 4, SLOT_SIZE, 0xFF222222);
        }
    }

    private void renderArrow(GuiGraphicsExtractor graphics, boolean leftHas, boolean rightHas) {
        int x1 = leftSlotX + SLOT_SIZE + 1;
        int x2 = rightSlotX - 2;
        int mid = slotY + SLOT_SIZE / 2;
        graphics.fill(x1, mid, x2, mid + 2, leftHas || rightHas ? 0xFF333333 : 0xFF777777);
    }

    private static Identifier lastOf(Set<Identifier> selected) {
        Identifier last = null;
        for (Identifier id : selected) {
            last = id;
        }
        return last;
    }

    private int maxScroll(int total) {
        int rows = (int) Math.ceil(total / (double) GRID_COLS);
        return Math.max(0, rows * CELL_H - GRID_H);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0) {
            if (handleGridClick(mouseX, mouseY, true) || handleGridClick(mouseX, mouseY, false)) {
                return true;
            }
            if (mouseX >= leftSlotX && mouseX < leftSlotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                removeLast(leftSelected);
                return true;
            }
            if (mouseX >= rightSlotX && mouseX < rightSlotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                removeLast(rightSelected);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean handleGridClick(double mouseX, double mouseY, boolean left) {
        int gridX = left ? leftGridX : rightGridX;
        if (mouseX < gridX || mouseX >= gridX + GRID_W - SCROLL_W || mouseY < gridY || mouseY >= gridY + GRID_H) {
            return false;
        }
        List<Identifier> source = sourceList(left);
        int scroll = left ? leftScroll : rightScroll;
        int column = (int) (mouseX - gridX) / CELL_W;
        int row = ((int) (mouseY - gridY) + scroll) / CELL_H;
        int index = row * GRID_COLS + column;
        if (column >= GRID_COLS || index < 0 || index >= source.size()) {
            return true;
        }
        Identifier id = source.get(index);
        Set<Identifier> selected = left ? leftSelected : rightSelected;
        if (!selected.add(id)) {
            selected.remove(id);
        }
        updateButtons();
        return true;
    }

    private void removeLast(Set<Identifier> selected) {
        if (selected.isEmpty()) {
            return;
        }
        Identifier last = lastOf(selected);
        selected.remove(last);
        updateButtons();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftGridX && mouseX < leftGridX + GRID_W && mouseY >= gridY && mouseY < gridY + GRID_H) {
            leftScroll = (int) Math.max(0, Math.min(maxScroll(sourceList(true).size()), leftScroll - scrollY * 12));
            return true;
        }
        if (mouseX >= rightGridX && mouseX < rightGridX + GRID_W && mouseY >= gridY && mouseY < gridY + GRID_H) {
            rightScroll = (int) Math.max(0, Math.min(maxScroll(sourceList(false).size()), rightScroll - scrollY * 12));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
