package com.astryxion.hats.client.gui.modern;

import net.minecraft.core.registries.BuiltInRegistries;

import com.astryxion.hats.client.gui.HatPreviewRenderer;
import com.astryxion.hats.client.hat.ClientHatUnlocks;
import com.astryxion.hats.client.gui.widget.HatsIcons;
import com.astryxion.hats.client.gui.widget.HatsWidgets;
import com.astryxion.hats.common.hat.HatAccessories;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.hat.HatRarity;
import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModernHatGridWidget implements GuiEventListener, NarratableEntry {

    public static final int CELL_W = 54;
    public static final int CELL_H = 66;
    public static final int SCROLL_W = 7;
    private static final int STRIP_W = 10;
    private static final int BTN_SZ = 11;
    private static final int BTN_GAP = 2;
    /** Entity previews sit at z 50. Keep the cell menu in front of the hat. */
    private static final int MENU_Z = 450;

    private final Minecraft minecraft;
    private final Font font;
    private final Consumer<@Nullable Item> onSelect;
    private final Consumer<ItemStack> onAccessoryStack;
    private final Supplier<ItemStack> stagedHat;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int cols;

    private int scrollOffset;
    private boolean scrollDragging;
    private int scrollDragStartY;
    private int scrollDragStartOffset;

    private final List<Item> allHats = new ArrayList<>();
    private final List<Item> filtered = new ArrayList<>();
    private String currentFilter = "";

    @Nullable
    private Item selected;
    private int hoveredIdx = -1;
    private int menuOpenIdx = -1;
    private boolean focused;
    private boolean visible = true;
    private boolean inAccessoryView;
    @Nullable
    private ItemStack accessoryViewStack;
    private int accScrollOffset;

    public ModernHatGridWidget(
            Minecraft minecraft,
            int x,
            int y,
            int width,
            int height,
            Consumer<@Nullable Item> onSelect,
            Consumer<ItemStack> onAccessoryStack,
            Supplier<ItemStack> stagedHat
    ) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
        this.onSelect = onSelect;
        this.onAccessoryStack = onAccessoryStack;
        this.stagedHat = stagedHat;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.cols = Math.max(1, gridW() / CELL_W);
    }

    private int gridW() {
        return width - SCROLL_W - 1;
    }

    private int totalRows() {
        return (int) Math.ceil(filtered.size() / (double) cols);
    }

    private int maxScroll() {
        return Math.max(0, totalRows() * CELL_H - height);
    }

    public void refreshHats(List<Item> hats) {
        allHats.clear();
        allHats.addAll(hats);
        applyFilter();
        menuOpenIdx = -1;
    }

    public boolean isAccessoryView() {
        return inAccessoryView;
    }

    public void closeAccessoryView() {
        inAccessoryView = false;
        accessoryViewStack = null;
        accScrollOffset = 0;
    }

    public void setFilter(String query) {
        currentFilter = query.toLowerCase(Locale.ROOT);
        applyFilter();
        scrollOffset = 0;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSelected(@Nullable Item item) {
        selected = item;
    }

    public List<Item> getVisibleHats() {
        return List.copyOf(filtered);
    }

    private void applyFilter() {
        filtered.clear();
        for (Item hat : allHats) {
            String name = Component.translatable(hat.getDescriptionId()).getString().toLowerCase(Locale.ROOT);
            if (currentFilter.isEmpty() || name.contains(currentFilter)) {
                filtered.add(hat);
            }
        }
    }

    public void render(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }
        if (inAccessoryView) {
            renderAccessoryView(gfx, mouseX, mouseY);
            return;
        }

        gfx.fill(x, y, x + gridW(), y + height, 0xFF3B3B3B);
        gfx.fill(x, y, x + gridW(), y + 1, 0xFF222222);
        gfx.fill(x, y, x + 1, y + height, 0xFF222222);
        gfx.fill(x, y + height - 1, x + gridW(), y + height, 0xFF666666);
        gfx.fill(x + gridW() - 1, y, x + gridW(), y + height, 0xFF666666);

        hoveredIdx = -1;
        if (isInGrid(mouseX, mouseY)) {
            int relX = (int) (mouseX - x);
            int relY = (int) (mouseY - y) + scrollOffset;
            int column = relX / CELL_W;
            int row = relY / CELL_H;
            int index = row * cols + column;
            if (column < cols && index >= 0 && index < filtered.size()) {
                hoveredIdx = index;
            }
        }

        if (filtered.isEmpty()) {
            gfx.text(font, Component.translatable("hats.gui.no_hats").getString(), x + 4, y + 4, 0xFF888888);
        } else {
            gfx.enableScissor(x + 1, y + 1, x + gridW() - 1, y + height - 1);
            Player player = minecraft.player;
            int row = 0;
            int column = 0;
            for (int i = 0; i < filtered.size(); i++) {
                Item hat = filtered.get(i);
                int cellX = x + column * CELL_W;
                int cellY = y + row * CELL_H - scrollOffset;
                if (cellY + CELL_H > y && cellY < y + height) {
                    renderCell(gfx, hat, i, cellX, cellY, mouseX, mouseY, player);
                }
                if (++column >= cols) {
                    column = 0;
                    row++;
                }
            }
            gfx.disableScissor();
        }

        renderScrollbar(gfx, mouseX, mouseY);

        if (hoveredIdx >= 0 && hoveredIdx < filtered.size() && menuOpenIdx == -1) {
            Item hat = filtered.get(hoveredIdx);
            HatRarity rarity = rarity(hat);
            List<Component> lines = new ArrayList<>();
            Component name = Component.translatable(hat.getDescriptionId()).copy()
                    .withStyle(style -> style.withColor(rarity.getColor()));
            lines.add(name);
            String rarityLabel = rarity.name().charAt(0) + rarity.name().substring(1).toLowerCase(Locale.ROOT);
            lines.add(Component.literal(rarityLabel).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            int copies = ClientHatUnlocks.count(BuiltInRegistries.ITEM.getKey(hat));
            if (copies > 1) {
                lines.add(Component.literal("x" + copies).withStyle(ChatFormatting.GRAY));
            }
            gfx.setTooltipForNextFrame(font, lines.stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
        }
    }

    private void renderCell(GuiGraphicsExtractor gfx, Item hat, int index, int cellX, int cellY, int mouseX, int mouseY, @Nullable Player player) {
        boolean selectedCell = selected == hat;
        boolean hovered = !scrollDragging && hoveredIdx == index;
        boolean menuOpen = menuOpenIdx == index;
        boolean favourite = ClientFavourites.isFavourite(hatId(hat));

        HatsWidgets.ButtonState state = selectedCell || menuOpen
                ? HatsWidgets.ButtonState.CLICK
                : hovered ? HatsWidgets.ButtonState.HOVER : HatsWidgets.ButtonState.IDLE;
        HatsWidgets.renderMinecraftStyleButton(gfx, cellX, cellY, CELL_W, CELL_H, state);

        if (favourite) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, HatsIcons.FAVOURITE, cellX + CELL_W - STRIP_W - 9, cellY + 2, 0.0F, 0.0F, 7, 7, 16, 16, 16, 16);
        }
        if (player != null) {
            renderPlayerInCell(gfx, hat, cellX, cellY, player);
        }
        renderVerticalName(gfx, hat, cellX, cellY);
        renderCount(gfx, hat, cellX + 3, cellY + CELL_H - 8);

        if (hovered || menuOpen) {
            renderHamburger(gfx, cellX, cellY, menuOpen, mouseX, mouseY);
        }
        if (menuOpen) {
            renderMenuOptions(gfx, hat, cellX, cellY, mouseX, mouseY);
        }
    }

    private void renderHamburger(GuiGraphicsExtractor gfx, int cellX, int cellY, boolean active, int mouseX, int mouseY) {
        int buttonX = cellX + 1;
        int buttonY = cellY + 1;
        boolean hovered = mouseX >= buttonX && mouseX < buttonX + BTN_SZ && mouseY >= buttonY && mouseY < buttonY + BTN_SZ;
        int background = active ? 0xCC1A4A8A : (hovered ? 0xCCAAAACC : 0xCC333355);
        gfx.fill(buttonX, buttonY, buttonX + BTN_SZ, buttonY + BTN_SZ, background);
        for (int line = 0; line < 3; line++) {
            gfx.fill(buttonX + 2, buttonY + 3 + line * 3, buttonX + BTN_SZ - 2, buttonY + 4 + line * 3, 0xFFDDDDDD);
        }
    }

    private void renderMenuOptions(GuiGraphicsExtractor gfx, Item hat, int cellX, int cellY, int mouseX, int mouseY) {
        boolean favourite = ClientFavourites.isFavourite(hatId(hat));
        int buttonX = cellX + 1;
        int optionY = cellY + 1 + BTN_SZ + BTN_GAP;
        if (hasAccessories(hat)) {
            renderOptionButton(gfx, buttonX, optionY, "+", 0xFF004488, mouseX, mouseY);
            optionY += BTN_SZ + BTN_GAP;
        }
        renderOptionButton(gfx, buttonX, optionY, favourite ? "\u2605" : "\u2606",
                favourite ? 0xFF885500 : 0xFF555555, mouseX, mouseY);
    }

    private void renderOptionButton(GuiGraphicsExtractor gfx, int buttonX, int buttonY, String label, int iconColor, int mouseX, int mouseY) {
        boolean hovered = mouseX >= buttonX && mouseX < buttonX + BTN_SZ && mouseY >= buttonY && mouseY < buttonY + BTN_SZ;
        gfx.fill(buttonX, buttonY, buttonX + BTN_SZ, buttonY + BTN_SZ, iconColor | 0xBB000000);
        gfx.fill(buttonX + 1, buttonY + 1, buttonX + BTN_SZ - 1, buttonY + BTN_SZ - 1, hovered ? 0xCCBBBBDD : 0xCC222244);
        gfx.pose().pushMatrix();
        gfx.pose().translate(buttonX + BTN_SZ / 2f, buttonY + BTN_SZ / 2f);
        gfx.pose().scale(0.7f, 0.7f);
        int textWidth = font.width(label);
        gfx.text(font, label, -textWidth / 2, -font.lineHeight / 2, 0xFFFFFFFF, true);
        gfx.pose().popMatrix();
    }

    private void renderPlayerInCell(GuiGraphicsExtractor gfx, Item hat, int cellX, int cellY, Player player) {
        int contentW = CELL_W - STRIP_W;
        HatPreviewRenderer.render(gfx, player, new ItemStack(hat),
                cellX + contentW / 2 + 1, cellY + CELL_H + 54, 42,
                cellX + 1, cellY + 1, cellX + contentW - 1, cellY + CELL_H - 1);
    }

    private static void renderCount(GuiGraphicsExtractor gfx, Item hat, int x, int y) {
        int copies = ClientHatUnlocks.count(BuiltInRegistries.ITEM.getKey(hat));
        if (copies <= 1) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        float scale = 0.6F;
        gfx.pose().pushMatrix();
        gfx.pose().translate(x, y - font.lineHeight * scale);
        gfx.pose().scale(scale, scale);
        gfx.text(font, "x" + copies, 0, 0, 0xFFFFFFFF, true);
        gfx.pose().popMatrix();
    }

    private void renderVerticalName(GuiGraphicsExtractor gfx, Item hat, int cellX, int cellY) {
        String name = Component.translatable(hat.getDescriptionId()).getString();
        int stripX = cellX + CELL_W - STRIP_W;
        gfx.fill(stripX, cellY + 1, cellX + CELL_W - 1, cellY + CELL_H - 1, 0x99000000);
        int color = rarity(hat).getColor() | 0xFF000000;

        float scale = 0.6f;
        int available = (int) ((CELL_H - 6) / scale);
        int textWidth = font.width(name);
        int yOff = -(int) (STRIP_W / scale / 2f + font.lineHeight / 2f - 1);

        gfx.enableScissor(stripX + 1, cellY + 2, cellX + CELL_W - 1, cellY + CELL_H - 2);
        gfx.pose().pushMatrix();
        gfx.pose().translate(cellX + CELL_W - 2, cellY + CELL_H - 3);
        gfx.pose().rotate((float) Math.toRadians(-90f));
        gfx.pose().scale(scale, scale);
        gfx.text(font, name, (int) (2 - scrollOffset(textWidth, available)), yOff, color, true);
        gfx.pose().popMatrix();
        gfx.disableScissor();
    }

    private void renderAccessoryView(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        if (accessoryViewStack == null || accessoryViewStack.isEmpty()) {
            return;
        }

        TabulaHatDefinition parent = TabulaHats.from(accessoryViewStack);
        List<TabulaHatDefinition> accessories = parent == null ? List.of() : parent.allAccessories();
        int totalW = gridW() + SCROLL_W + 1;
        gfx.fill(x, y, x + totalW, y + height, 0xFF3B3B3B);

        int mainW = totalW * 2 / 5;
        int backBtnH = 16;
        int mainContentH = height - backBtnH - 2;
        renderBigCard(gfx, x, y, mainW, mainContentH, true, false);
        if (minecraft.player != null) {
            renderPlayerInCard(gfx, accessoryViewStack, x, y, mainW, mainContentH, minecraft.player);
        }
        renderVerticalNameInArea(gfx, accessoryViewStack.getHoverName().getString(),
                rarity(accessoryViewStack.getItem()).getColor() | 0xFF000000, x, y, mainW, mainContentH);

        int backY = y + height - backBtnH;
        boolean backHovered = mouseX >= x + 1 && mouseX < x + mainW - 1
                && mouseY >= backY + 1 && mouseY < backY + backBtnH - 1;
        gfx.fill(x + 1, backY + 1, x + mainW - 1, backY + backBtnH - 1, backHovered ? 0xFFAAAAAA : 0xFF777777);
        gfx.fill(x + 1, backY, x + mainW - 1, backY + 1, 0xFF555555);
        gfx.text(font, Component.translatable("hats.gui.back"), x + 4, backY + 4, 0xFFFFFFFF, false);

        int accX = x + mainW + 1;
        int accW = totalW - mainW - 1 - SCROLL_W;
        if (accessories.isEmpty()) {
            gfx.text(font, Component.translatable("hats.gui.noAccessories"), accX + 4, y + 4, 0xFF888888, false);
            return;
        }

        int accH = Math.max(CELL_H, height / Math.max(1, accessories.size()));
        int maxAccScroll = Math.max(0, accessories.size() * accH - height);
        accScrollOffset = Math.min(accScrollOffset, maxAccScroll);
        Player player = minecraft.player;

        gfx.enableScissor(accX, y, accX + accW, y + height);
        for (int i = 0; i < accessories.size(); i++) {
            TabulaHatDefinition accessory = accessories.get(i);
            int accY = y + i * accH - accScrollOffset;
            if (accY + accH <= y || accY >= y + height) {
                continue;
            }
            boolean enabled = HatAccessories.isShowing(accessoryViewStack, accessory.id);
            boolean blocked = HatAccessories.isBlocked(accessoryViewStack, parent, accessory.id);
            boolean hovered = !scrollDragging && !blocked
                    && mouseX >= accX && mouseX < accX + accW
                    && mouseY >= accY && mouseY < accY + accH
                    && mouseY >= y && mouseY < y + height;
            renderBigCard(gfx, accX, accY, accW, accH, false, hovered);
            if (enabled) {
                gfx.fill(accX + 1, accY + 1, accX + 5, accY + accH - 1, 0xFF44FF44);
            }
            if (blocked) {
                gfx.fill(accX + 1, accY + 1, accX + accW - 1, accY + accH - 1, 0x88000000);
            }
            if (player != null) {
                renderPlayerInCard(gfx, accessoryPreview(parent, accessory), accX, accY, accW, accH, player);
            }
            renderVerticalNameInArea(gfx, accessory.displayName, accessory.rarity.getColor() | 0xFF000000, accX, accY, accW, accH);
        }
        gfx.disableScissor();

        if (maxAccScroll > 0) {
            int barX = accX + accW;
            gfx.fill(barX, y, barX + SCROLL_W, y + height, 0xFF2A2A2A);
            gfx.fill(barX + 1, y + 1, barX + SCROLL_W - 1, y + height - 1, 0xFF333333);
            int inner = height - 2;
            int thumbH = Math.max(14, inner * inner / (inner + maxAccScroll));
            int thumbY = y + 1 + (int) ((inner - thumbH) * (float) accScrollOffset / maxAccScroll);
            gfx.fill(barX + 1, thumbY, barX + SCROLL_W - 1, thumbY + thumbH, 0xFF777777);
        }
    }

    private ItemStack accessoryPreview(TabulaHatDefinition parent, TabulaHatDefinition accessory) {
        ItemStack preview = accessoryViewStack.copy();
        for (TabulaHatDefinition other : parent.allAccessories()) {
            HatAccessories.setShowing(preview, parent, other.id, false);
        }
        HatAccessories.setShowing(preview, parent, accessory.id, true);
        return preview;
    }

    private void renderBigCard(GuiGraphicsExtractor gfx, int cellX, int cellY, int cellW, int cellH, boolean selected, boolean hovered) {
        int background = selected ? 0xFF1A3A6A : (hovered ? 0xFF505060 : 0xFF404040);
        gfx.fill(cellX, cellY, cellX + cellW, cellY + cellH, background);
        gfx.fill(cellX, cellY, cellX + cellW, cellY + 1, 0xFF666666);
        gfx.fill(cellX, cellY, cellX + 1, cellY + cellH, 0xFF666666);
        gfx.fill(cellX, cellY + cellH - 1, cellX + cellW, cellY + cellH, 0xFF222222);
        gfx.fill(cellX + cellW - 1, cellY, cellX + cellW, cellY + cellH, 0xFF222222);
    }

    private void renderPlayerInCard(GuiGraphicsExtractor gfx, ItemStack hat, int cellX, int cellY, int cellW, int cellH, Player player) {
        int previewBottom = cellY + cellH - 1;
        int previewH = Math.max(1, cellH - 2);
        int scale = Math.max(32, Math.round(previewH / 1.2f));
        HatPreviewRenderer.render(gfx, player, hat.copy(),
                cellX + (cellW - STRIP_W) / 2,
                previewBottom + Math.round(1.35f * scale),
                scale,
                cellX + 1, cellY + 1, cellX + cellW - STRIP_W - 1, previewBottom);
    }

    private void renderVerticalNameInArea(GuiGraphicsExtractor gfx, String name, int color, int cellX, int cellY, int cellW, int cellH) {
        int stripX = cellX + cellW - STRIP_W;
        gfx.fill(stripX, cellY + 1, cellX + cellW - 1, cellY + cellH - 1, 0x99000000);
        float scale = 0.6f;
        int available = (int) ((cellH - 6) / scale);
        int textWidth = font.width(name);
        int yOff = -(int) (STRIP_W / scale / 2f + font.lineHeight / 2f - 1);
        gfx.enableScissor(stripX + 1, cellY + 2, cellX + cellW - 1, cellY + cellH - 2);
        gfx.pose().pushMatrix();
        gfx.pose().translate(cellX + cellW - 2, cellY + cellH - 3);
        gfx.pose().rotate((float) Math.toRadians(-90f));
        gfx.pose().scale(scale, scale);
        gfx.text(font, name, (int) (2 - scrollOffset(textWidth, available)), yOff, color, true);
        gfx.pose().popMatrix();
        gfx.disableScissor();
    }

    private static HatRarity rarity(Item hat) {
        return hat instanceof HatItem hatItem ? hatItem.getHatRarity() : HatRarity.COMMON;
    }

    private float scrollOffset(int textWidth, int available) {
        if (textWidth <= available) {
            return 0f;
        }
        float overflow = textWidth - available;
        float scrollMs = overflow / 30f * 1000f;
        float pauseMs = 1000f;
        long period = Math.max(100L, (long) (2 * (scrollMs + pauseMs)));
        long time = System.currentTimeMillis() % period;
        if (time < pauseMs) {
            return 0f;
        }
        if (time < pauseMs + scrollMs) {
            return (time - pauseMs) / scrollMs * overflow;
        }
        if (time < 2 * pauseMs + scrollMs) {
            return overflow;
        }
        return (1f - (time - 2 * pauseMs - scrollMs) / scrollMs) * overflow;
    }

    private void renderScrollbar(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        int barX = x + gridW();
        gfx.fill(barX, y, barX + SCROLL_W, y + height, 0xFF2A2A2A);
        gfx.fill(barX + 1, y + 1, barX + SCROLL_W - 1, y + height - 1, 0xFF333333);
        int max = maxScroll();
        if (max <= 0) {
            return;
        }
        int inner = height - 2;
        int thumbH = Math.max(14, inner * inner / (inner + max));
        int thumbY = y + 1 + (int) ((inner - thumbH) * (float) scrollOffset / max);
        boolean over = mouseX >= barX && mouseX < barX + SCROLL_W && mouseY >= thumbY && mouseY < thumbY + thumbH;
        int color = (scrollDragging || over) ? 0xFFAAAAAA : 0xFF777777;
        gfx.fill(barX + 1, thumbY, barX + SCROLL_W - 1, thumbY + thumbH, color);
        gfx.fill(barX + 1, thumbY, barX + SCROLL_W - 1, thumbY + 1, 0xFFCCCCCC);
        gfx.fill(barX + 1, thumbY, barX + 2, thumbY + thumbH, 0xFFCCCCCC);
    }

    private int thumbH() {
        int inner = height - 2;
        int max = maxScroll();
        return max <= 0 ? inner : Math.max(14, inner * inner / (inner + max));
    }

    private boolean isInGrid(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + gridW() && mouseY >= y && mouseY < y + height;
    }

    private boolean isInScrollbar(double mouseX, double mouseY) {
        int barX = x + gridW();
        return mouseX >= barX && mouseX < barX + SCROLL_W && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (!visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (button == 0 && inAccessoryView) {
            return handleAccessoryViewClick(mouseX, mouseY);
        }
        if (button == 0 && isInScrollbar(mouseX, mouseY)) {
            scrollDragging = true;
            scrollDragStartY = (int) mouseY;
            scrollDragStartOffset = scrollOffset;
            return true;
        }
        if (!isInGrid(mouseX, mouseY)) {
            return false;
        }

        int column = (int) (mouseX - x) / CELL_W;
        int row = ((int) (mouseY - y) + scrollOffset) / CELL_H;
        int index = row * cols + column;
        if (column >= cols || index < 0 || index >= filtered.size()) {
            menuOpenIdx = -1;
            return true;
        }

        int cellX = x + column * CELL_W;
        int cellY = y + row * CELL_H - scrollOffset;
        if (button == 0) {
            if (mouseX >= cellX + 1 && mouseX < cellX + 1 + BTN_SZ
                    && mouseY >= cellY + 1 && mouseY < cellY + 1 + BTN_SZ) {
                menuOpenIdx = menuOpenIdx == index ? -1 : index;
                return true;
            }
            if (menuOpenIdx == index && handleMenuOptionClick(filtered.get(index), cellX, cellY, mouseX, mouseY)) {
                return true;
            }
        }

        menuOpenIdx = -1;
        Item clicked = filtered.get(index);
        selected = selected == clicked ? null : clicked;
        onSelect.accept(selected);
        return true;
    }

    private boolean handleMenuOptionClick(Item hat, int cellX, int cellY, double mouseX, double mouseY) {
        int buttonX = cellX + 1;
        int optionY = cellY + 1 + BTN_SZ + BTN_GAP;
        if (hasAccessories(hat)) {
            if (mouseX >= buttonX && mouseX < buttonX + BTN_SZ && mouseY >= optionY && mouseY < optionY + BTN_SZ) {
                enterAccessoryView(hat);
                return true;
            }
            optionY += BTN_SZ + BTN_GAP;
        }
        if (mouseX >= buttonX && mouseX < buttonX + BTN_SZ && mouseY >= optionY && mouseY < optionY + BTN_SZ) {
            ClientFavourites.toggle(hatId(hat));
            menuOpenIdx = -1;
            return true;
        }
        return false;
    }

    private boolean handleAccessoryViewClick(double mouseX, double mouseY) {
        if (accessoryViewStack == null) {
            return false;
        }
        TabulaHatDefinition parent = TabulaHats.from(accessoryViewStack);
        List<TabulaHatDefinition> accessories = parent == null ? List.of() : parent.allAccessories();
        int totalW = gridW() + SCROLL_W + 1;
        int mainW = totalW * 2 / 5;
        int backBtnH = 16;
        int backY = y + height - backBtnH;
        if (mouseX >= x + 1 && mouseX < x + mainW - 1
                && mouseY >= backY + 1 && mouseY < backY + backBtnH - 1) {
            closeAccessoryView();
            return true;
        }

        int accX = x + mainW + 1;
        int accW = totalW - mainW - 1 - SCROLL_W;
        int accH = accessories.isEmpty() ? height : Math.max(CELL_H, height / Math.max(1, accessories.size()));
        if (mouseX >= accX && mouseX < accX + accW && mouseY >= y && mouseY < y + height) {
            int index = ((int) mouseY - y + accScrollOffset) / accH;
            if (index >= 0 && index < accessories.size() && parent != null) {
                TabulaHatDefinition accessory = accessories.get(index);
                if (HatAccessories.isBlocked(accessoryViewStack, parent, accessory.id)) {
                    return true;
                }
                HatAccessories.toggle(accessoryViewStack, parent, accessory.id);
                onAccessoryStack.accept(accessoryViewStack.copy());
            }
        }
        return true;
    }

    private void enterAccessoryView(Item hat) {
        ItemStack current = stagedHat.get();
        if (!current.isEmpty() && current.getItem() == hat) {
            accessoryViewStack = current.copy();
        } else {
            accessoryViewStack = new ItemStack(hat);
        }
        selected = hat;
        menuOpenIdx = -1;
        accScrollOffset = 0;
        inAccessoryView = true;
        onSelect.accept(hat);
        onAccessoryStack.accept(accessoryViewStack.copy());
    }

    private static boolean hasAccessories(Item hat) {
        if (!(hat instanceof HatItem hatItem) || !hatItem.isTabula()) {
            return false;
        }
        TabulaHatDefinition def = TabulaHats.get(hatItem.getHatId());
        return def != null && !def.allAccessories().isEmpty();
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int button = event.button();
        if (button == 0) {
            scrollDragging = false;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseY = event.y();
        int button = event.button();
        if (!visible || button != 0 || !scrollDragging) {
            return false;
        }
        int max = maxScroll();
        int travel = height - 2 - thumbH();
        if (travel <= 0) {
            return true;
        }
        int delta = (int) mouseY - scrollDragStartY;
        scrollOffset = (int) Math.max(0, Math.min(max, scrollDragStartOffset + (long) delta * max / travel));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (inAccessoryView) {
            List<TabulaHatDefinition> accessories = accessoryViewAccessories();
            int accH = Math.max(CELL_H, height / Math.max(1, accessories.size()));
            int maxAccScroll = Math.max(0, accessories.size() * accH - height);
            accScrollOffset = (int) Math.max(0, Math.min(maxAccScroll, accScrollOffset - scrollY * 12));
            return true;
        }
        scrollOffset = (int) Math.max(0, Math.min(maxScroll(), scrollOffset - scrollY * 12));
        return true;
    }

    private List<TabulaHatDefinition> accessoryViewAccessories() {
        TabulaHatDefinition parent = TabulaHats.from(accessoryViewStack);
        return parent == null ? List.of() : parent.allAccessories();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

    public static String hatId(Item hat) {
        var id = BuiltInRegistries.ITEM.getKey(hat);
        return id != null ? id.toString() : Component.translatable(hat.getDescriptionId()).getString();
    }
}
