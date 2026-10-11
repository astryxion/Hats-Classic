package com.astryxion.hats.client.gui;

import com.astryxion.hats.client.gui.modern.ClientFavourites;
import com.astryxion.hats.client.gui.modern.ModernGuiCamera;
import com.astryxion.hats.client.gui.modern.ModernHatGridWidget;
import com.astryxion.hats.client.gui.widget.HatsButton;
import com.astryxion.hats.client.gui.widget.HatsIconButton;
import com.astryxion.hats.client.gui.widget.HatsIcons;
import com.astryxion.hats.client.gui.widget.HatsToggleButton;
import com.astryxion.hats.common.equip.HatEquipController;
import com.astryxion.hats.common.hat.HatColor;
import com.astryxion.hats.common.hat.HatGlint;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hatstand.HatStandEntity;
import com.astryxion.hats.common.hat.HatRarity;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketUpdateHatStand;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class ModernHatsScreen extends Screen {

    private static final int PANEL_PAD = 6;
    private static final int PANEL_INSET = 4;
    private static final int SCREEN_MARGIN = 5;
    private static final int GRID_COLS = 3;
    private static final int FIXED_PANEL_W = GRID_COLS * ModernHatGridWidget.CELL_W
            + ModernHatGridWidget.SCROLL_W + 1
            + (PANEL_INSET + PANEL_PAD) * 2;
    private static final int SIDE_BTN_SIZE = 20;
    private static final int SIDE_BTN_GAP = 4;
    private static final int FORCED_SCALE = 4;
    private static final int CATEGORY_MENU_W = 92;
    private static final int CATEGORY_ITEM_H = 13;
    private static final int CATEGORY_PAD = 4;

    private final Random random = new Random();

    @Nullable
    private ModernHatGridWidget hatGrid;
    @Nullable
    private EditBox searchBox;
    @Nullable
    private Button equipButton;
    @Nullable
    private Button removeButton;
    @Nullable
    private Button categoryButton;
    @Nullable
    private HatsButton resetColorButton;
    @Nullable
    private HatsIconButton tradeButton;
    @Nullable
    private Item selectedHat;

    private ItemStack equippedHat = ItemStack.EMPTY;
    private ItemStack stagedHat = ItemStack.EMPTY;
    private boolean stateCaptured;
    private boolean modeAwardSent;
    private boolean colorizerOpen;
    private boolean favouritesOnly;
    private boolean categoryMenuOpen;
    @Nullable
    private HatRarity category;
    private String searchQuery = "";
    private int savedGuiScale = -1;
    private int unlockGeneration;
    private final int standEntityId;

    public ModernHatsScreen() {
        this(-1);
    }

    public ModernHatsScreen(int standEntityId) {
        super(Component.translatable("hats.gui.title"));
        this.standEntityId = standEntityId;
    }

    private int panelX() {
        return width - SCREEN_MARGIN - FIXED_PANEL_W;
    }

    public LivingEntity previewTarget() {
        if (minecraft == null || minecraft.level == null) {
            return null;
        }
        if (standEntityId >= 0) {
            return minecraft.level.getEntity(standEntityId) instanceof HatStandEntity stand ? stand : null;
        }
        return minecraft.player;
    }

    public int standEntityId() {
        return standEntityId;
    }

    private int panelY() {
        return SCREEN_MARGIN;
    }

    private int panelW() {
        return FIXED_PANEL_W;
    }

    private int panelH() {
        return height - SCREEN_MARGIN * 2;
    }

    private int innerX() {
        return panelX() + PANEL_INSET + PANEL_PAD;
    }

    private int innerW() {
        return panelW() - (PANEL_INSET + PANEL_PAD) * 2;
    }

    private int innerY() {
        return panelY() + PANEL_INSET + PANEL_PAD;
    }

    private int sideX() {
        return panelX() - SCREEN_MARGIN - SIDE_BTN_SIZE;
    }

    private boolean isInPreviewArea(double mouseX) {
        return mouseX < sideX();
    }

    @Override
    protected void init() {
        int autoScale = minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode());
        int effectiveScale = Math.min(FORCED_SCALE, autoScale);
        if (savedGuiScale == -1) {
            savedGuiScale = (int) Math.round(minecraft.getWindow().getGuiScale());
        }
        minecraft.getWindow().setGuiScale(effectiveScale);
        width = minecraft.getWindow().getGuiScaledWidth();
        height = minecraft.getWindow().getGuiScaledHeight();

        LivingEntity target = previewTarget();
        if (target != null && !stateCaptured) {
            equippedHat = HatManager.getHat(target).copy();
            stagedHat = equippedHat.copy();
            stateCaptured = true;
        }
        if (!modeAwardSent) {
            modeAwardSent = true;
            HatsGui.notifyMenuOpened(standEntityId >= 0);
        }

        int searchH = 18;
        int buttonH = 20;
        int gridTop = innerY() + searchH + PANEL_PAD;
        int gridBottom = panelY() + panelH() - PANEL_INSET - PANEL_PAD - buttonH - PANEL_PAD;

        searchBox = addRenderableWidget(new EditBox(
                font, innerX(), innerY(), innerW(), searchH, Component.translatable("hats.gui.search")));
        searchBox.setHint(Component.translatable("hats.gui.search"));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(query -> {
            searchQuery = query;
            if (hatGrid != null) {
                hatGrid.setFilter(query);
            }
        });
        searchBox.visible = !colorizerOpen;

        hatGrid = new ModernHatGridWidget(
                minecraft, innerX(), gridTop, innerW(), gridBottom - gridTop,
                this::onHatSelected, this::onAccessoryStack, () -> stagedHat);
        unlockGeneration = com.astryxion.hats.client.hat.ClientHatUnlocks.generation();
        hatGrid.refreshHats(buildHatList());
        hatGrid.setFilter(searchQuery);
        hatGrid.setSelected(selectedHat);
        hatGrid.setVisible(!colorizerOpen);
        addWidget(hatGrid);

        if (colorizerOpen) {
            addColorizer();
        }

        int buttonY = panelY() + panelH() - PANEL_INSET - PANEL_PAD - buttonH;
        equipButton = addRenderableWidget(HatsButton.create(
                innerX(), buttonY, innerW(), buttonH,
                Component.translatable("hats.gui.equip"), button -> equipSelected()));

        addSidebarButtons();

        updateButtons();
        previewStaged();
    }

    private void addSidebarButtons() {
        int x = sideX();
        int y = innerY();
        int step = SIDE_BTN_SIZE + SIDE_BTN_GAP;

        removeButton = addRenderableWidget(new HatsIconButton(
                x, y, SIDE_BTN_SIZE, SIDE_BTN_SIZE, HatsIcons.CANCEL,
                Component.translatable("hats.gui.removeHat"), button -> removeEquipped()));

        addRenderableWidget(new HatsIconButton(
                x, y + step, SIDE_BTN_SIZE, SIDE_BTN_SIZE,
                colorizerOpen ? HatsIcons.HAT : HatsIcons.COLOURISE,
                Component.translatable(colorizerOpen ? "hats.gui.hatsList" : "hats.gui.colorizer"),
                button -> toggleColorizer()));

        addRenderableWidget(new HatsIconButton(
                x, y + step * 2, SIDE_BTN_SIZE, SIDE_BTN_SIZE, HatsIcons.RANDOMISE,
                Component.translatable("hats.gui.randomHat"), button -> randomizeHat()));

        addRenderableWidget(new HatsIconButton(
                x, y + step * 3, SIDE_BTN_SIZE, SIDE_BTN_SIZE,
                favouritesOnly ? HatsIcons.HAT : HatsIcons.FAVOURITE,
                Component.translatable(favouritesOnly ? "hats.gui.hatsList" : "hats.gui.favorites"),
                button -> handleFavourites()));

        categoryButton = addRenderableWidget(new HatsIconButton(
                x, y + step * 4, SIDE_BTN_SIZE, SIDE_BTN_SIZE,
                category != null ? HatsIcons.HAT : HatsIcons.CATEGORIES,
                Component.translatable(category != null ? "hats.gui.hatsList" : "hats.gui.categories"),
                button -> toggleCategories()));

        addRenderableWidget(new HatsIconButton(
                x, y + step * 5, SIDE_BTN_SIZE, SIDE_BTN_SIZE, HatsIcons.PERSONALISE,
                Component.translatable("hats.gui.personalize"),
                button -> minecraft.setScreen(new HatConfigScreen(this))));

        if (standEntityId < 0) {
            tradeButton = addRenderableWidget(new HatsIconButton(
                    x, y + step * 6, SIDE_BTN_SIZE, SIDE_BTN_SIZE, HatsIcons.TRADE,
                    Component.translatable("hats.gui.trade"),
                    button -> com.astryxion.hats.client.trade.ClientTradeFlow.openTrade()));
        } else {
            tradeButton = null;
        }
    }

    private void addColorizer() {
        int x = innerX();
        int y = innerY();
        int width = innerW();
        boolean enabled = !stagedHat.isEmpty();

        ColorSlider red = addRenderableWidget(new ColorSlider(
                x, y, width, "hats.gui.red", HatColor.red(stagedHat),
                value -> setColor(value, null, null, null)));
        ColorSlider green = addRenderableWidget(new ColorSlider(
                x, y + 22, width, "hats.gui.green", HatColor.green(stagedHat),
                value -> setColor(null, value, null, null)));
        ColorSlider blue = addRenderableWidget(new ColorSlider(
                x, y + 44, width, "hats.gui.blue", HatColor.blue(stagedHat),
                value -> setColor(null, null, value, null)));
        ColorSlider alpha = addRenderableWidget(new ColorSlider(
                x, y + 66, width, "hats.gui.alpha", HatColor.alpha(stagedHat),
                value -> setColor(null, null, null, value)));
        red.active = green.active = blue.active = alpha.active = enabled;

        HatsToggleButton glint = addRenderableWidget(new HatsToggleButton(
                x, y + 88, width, 20,
                Component.translatable("hats.gui.glint"),
                HatGlint.get(stagedHat),
                "hats.gui.glint.tooltip",
                value -> {
                    HatGlint.set(stagedHat, value);
                    previewStaged();
                }));
        glint.active = enabled;

        resetColorButton = addRenderableWidget(HatsButton.create(
                x, y + 110, width, 20, Component.translatable("hats.gui.resetColor"),
                button -> {
                    HatColor.set(stagedHat, 255, 255, 255, 255);
                    previewStaged();
                    rebuildWidgets();
                }));
        resetColorButton.active = enabled && !HatColor.isDefault(stagedHat);
    }

    private List<Item> buildHatList() {
        List<Item> hats = new ArrayList<>(HatsGui.unlockedHats(minecraft.player));
        hats.removeIf(item -> {
            if (favouritesOnly && !ClientFavourites.isFavourite(ModernHatGridWidget.hatId(item))) {
                return true;
            }
            return category != null && rarity(item) != category;
        });
        hats.sort(Comparator
                .comparing((Item item) -> !ClientFavourites.isFavourite(ModernHatGridWidget.hatId(item)))
                .thenComparing(item -> Component.translatable(item.getDescriptionId()).getString(), String.CASE_INSENSITIVE_ORDER));
        return hats;
    }

    private static HatRarity rarity(Item item) {
        return item instanceof HatItem hatItem ? hatItem.getHatRarity() : HatRarity.COMMON;
    }

    @Override
    public void tick() {
        super.tick();
        int generation = com.astryxion.hats.client.hat.ClientHatUnlocks.generation();
        if (generation != unlockGeneration) {
            unlockGeneration = generation;
            refreshGrid();
        }
    }

    private void refreshGrid() {
        if (hatGrid != null) {
            hatGrid.refreshHats(buildHatList());
            hatGrid.setFilter(searchQuery);
            hatGrid.setSelected(selectedHat);
        }
    }

    private void onHatSelected(@Nullable Item hat) {
        selectedHat = hat;
        if (hat != null && !stagedHat.isEmpty() && stagedHat.getItem() == hat) {
            previewStaged();
            updateButtons();
            return;
        }
        stagedHat = hat == null ? equippedHat.copy() : new ItemStack(hat);
        previewStaged();
        updateButtons();
    }

    private void onAccessoryStack(ItemStack stack) {
        stagedHat = stack.copy();
        selectedHat = stack.getItem();
        if (hatGrid != null) {
            hatGrid.setSelected(selectedHat);
        }
        previewStaged();
        updateButtons();
    }

    private void previewStaged() {
        LivingEntity target = previewTarget();
        if (target == null) {
            return;
        }
        if (stagedHat.isEmpty()) {
            HatManager.clearHat(target);
        } else {
            HatManager.setHatStack(target, stagedHat.copy());
        }
    }

    private void equipSelected() {
        if (minecraft.player == null || stagedHat.isEmpty()) {
            return;
        }
        if (standEntityId >= 0) {
            HatPacketHandler.sendToServer(new PacketUpdateHatStand(standEntityId, stagedHat.copy()));
            HatManager.setHatStack(previewTarget(), stagedHat.copy());
        } else {
            HatEquipController.equipStack(minecraft.player, stagedHat);
        }
        equippedHat = stagedHat.copy();
        updateButtons();
    }

    private void removeEquipped() {
        if (minecraft.player == null) {
            return;
        }
        if (standEntityId >= 0) {
            HatPacketHandler.sendToServer(new PacketUpdateHatStand(standEntityId, ItemStack.EMPTY));
            HatManager.clearHat(previewTarget());
        } else {
            HatEquipController.unequip(minecraft.player);
        }
        equippedHat = ItemStack.EMPTY;
        stagedHat = ItemStack.EMPTY;
        selectedHat = null;
        colorizerOpen = false;
        if (hatGrid != null) {
            hatGrid.closeAccessoryView();
            hatGrid.setSelected(null);
        }
        previewStaged();
        updateButtons();
    }

    private void toggleColorizer() {
        if (!colorizerOpen && stagedHat.isEmpty() && !equippedHat.isEmpty()) {
            stagedHat = equippedHat.copy();
            selectedHat = stagedHat.getItem();
        }
        colorizerOpen = !colorizerOpen;
        categoryMenuOpen = false;
        rebuildWidgets();
    }

    private void randomizeHat() {
        if (hatGrid == null) {
            return;
        }
        List<Item> hats = hatGrid.getVisibleHats();
        if (hats.isEmpty()) {
            return;
        }
        Item chosen = hats.get(random.nextInt(hats.size()));
        hatGrid.closeAccessoryView();
        onHatSelected(chosen);
        hatGrid.setSelected(chosen);
    }

    private void handleFavourites() {
        favouritesOnly = !favouritesOnly;
        categoryMenuOpen = false;
        rebuildWidgets();
    }

    private void toggleCategories() {
        if (category != null) {
            category = null;
            categoryMenuOpen = false;
            rebuildWidgets();
            return;
        }
        categoryMenuOpen = !categoryMenuOpen;
    }

    private void setColor(Integer red, Integer green, Integer blue, Integer alpha) {
        if (stagedHat.isEmpty()) {
            return;
        }
        HatColor.set(
                stagedHat,
                red != null ? red : HatColor.red(stagedHat),
                green != null ? green : HatColor.green(stagedHat),
                blue != null ? blue : HatColor.blue(stagedHat),
                alpha != null ? alpha : HatColor.alpha(stagedHat)
        );
        previewStaged();
        if (resetColorButton != null) {
            resetColorButton.active = !HatColor.isDefault(stagedHat);
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean equipped = !stagedHat.isEmpty() && ItemStack.isSameItemSameComponents(stagedHat, equippedHat);
        if (equipButton != null) {
            equipButton.active = !stagedHat.isEmpty() && !equipped;
            equipButton.setMessage(Component.translatable(equipped ? "hats.gui.equipped" : "hats.gui.equip"));
        }
        if (removeButton != null) {
            removeButton.active = !stagedHat.isEmpty() || !equippedHat.isEmpty();
        }
    }

    @Override
    public void removed() {
        restorePreview();
        restoreScale();
        super.removed();
    }

    private void restorePreview() {
        LivingEntity target = previewTarget();
        if (target == null) {
            return;
        }
        if (equippedHat.isEmpty()) {
            HatManager.clearHat(target);
        } else {
            HatManager.setHatStack(target, equippedHat.copy());
        }
    }

    private void restoreScale() {
        if (savedGuiScale != -1 && minecraft != null) {
            minecraft.getWindow().setGuiScale(savedGuiScale);
            savedGuiScale = -1;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        drawPanel(graphics, panelX(), panelY(), panelW(), panelH());
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (hatGrid != null) {
            hatGrid.render(graphics, mouseX, mouseY, partialTick);
        }
        if (categoryMenuOpen) {
            renderCategoryMenu(graphics, mouseX, mouseY);
        }
        if (tradeButton != null && com.astryxion.hats.client.trade.ClientTradeState.hasIncomingOffers()) {
            graphics.fill(tradeButton.getX() + tradeButton.getWidth() - 5, tradeButton.getY() + 1,
                    tradeButton.getX() + tradeButton.getWidth() - 1, tradeButton.getY() + 5, 0xFFFF3333);
        }
    }

    private void renderCategoryMenu(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (categoryButton == null) {
            return;
        }
        int menuH = CATEGORY_PAD * 2 + HatRarity.values().length * CATEGORY_ITEM_H;
        int menuX = categoryButton.getX() - CATEGORY_MENU_W - 2;
        int menuY = categoryButton.getY();
        graphics.fill(menuX - 1, menuY - 1, menuX + CATEGORY_MENU_W + 1, menuY + menuH + 1, 0xFF555555);
        graphics.fill(menuX, menuY, menuX + CATEGORY_MENU_W, menuY + menuH, 0xFF2B2B2B);
        HatRarity[] rarities = HatRarity.values();
        for (int i = 0; i < rarities.length; i++) {
            HatRarity rarity = rarities[i];
            int itemY = menuY + CATEGORY_PAD + i * CATEGORY_ITEM_H;
            boolean hovered = mouseX >= menuX && mouseX < menuX + CATEGORY_MENU_W
                    && mouseY >= itemY && mouseY < itemY + CATEGORY_ITEM_H;
            if (hovered) {
                graphics.fill(menuX, itemY, menuX + CATEGORY_MENU_W, itemY + CATEGORY_ITEM_H, 0xFF3D5A80);
            }
            String label = rarity.name().charAt(0) + rarity.name().substring(1).toLowerCase();
            graphics.text(font, label, menuX + CATEGORY_PAD, itemY + 2, rarity.getColor() | 0xFF000000, false);
        }
    }

    private void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF8B8B8B);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFFFFFFFF);
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555);
        graphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF555555);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0 && categoryMenuOpen && categoryButton != null) {
            int menuH = CATEGORY_PAD * 2 + HatRarity.values().length * CATEGORY_ITEM_H;
            int menuX = categoryButton.getX() - CATEGORY_MENU_W - 2;
            int menuY = categoryButton.getY();
            if (mouseX >= menuX && mouseX < menuX + CATEGORY_MENU_W
                    && mouseY >= menuY && mouseY < menuY + menuH) {
                int index = (int) (mouseY - menuY - CATEGORY_PAD) / CATEGORY_ITEM_H;
                if (index >= 0 && index < HatRarity.values().length) {
                    category = HatRarity.values()[index];
                    categoryMenuOpen = false;
                    rebuildWidgets();
                }
                return true;
            }
            categoryMenuOpen = false;
        }
        if (hatGrid != null && hatGrid.mouseClicked(event, doubleClick)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (hatGrid != null) {
            hatGrid.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0 && isInPreviewArea(mouseX)) {
            if (minecraft.hasShiftDown()) {
                ModernGuiCamera.guiPanX = Mth.clamp(ModernGuiCamera.guiPanX - (float) (dragX * 0.01), -2f, 2f);
                ModernGuiCamera.guiPanY = Mth.clamp(ModernGuiCamera.guiPanY + (float) (dragY * 0.01), -2f, 2f);
            } else {
                ModernGuiCamera.guiYaw += (float) (dragX * 0.5);
                ModernGuiCamera.guiPitch = Mth.clamp(ModernGuiCamera.guiPitch + (float) (dragY * 0.25), -85f, 85f);
            }
            return true;
        }
        if (hatGrid != null && hatGrid.mouseDragged(event, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isInPreviewArea(mouseX)) {
            ModernGuiCamera.guiZoom = Mth.clamp(
                    ModernGuiCamera.guiZoom + (float) (scrollY * 0.12), 0f, 1.5f);
            return true;
        }
        if (hatGrid != null && hatGrid.isMouseOver(mouseX, mouseY)) {
            return hatGrid.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft != null) {
            this.minecraft.gui.extractDeferredSubtitles();
        }
    }

    private final class ColorSlider extends AbstractSliderButton {

        private final String translationKey;
        private final java.util.function.IntConsumer consumer;

        private ColorSlider(int x, int y, int width, String translationKey, int channel,
                java.util.function.IntConsumer consumer) {
            super(x, y, width, 20, Component.empty(), channel / 255.0D);
            this.translationKey = translationKey;
            this.consumer = consumer;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(translationKey)
                    .append(": " + (int) Math.round(value * 255.0D)));
        }

        @Override
        protected void applyValue() {
            consumer.accept((int) Math.round(value * 255.0D));
        }
    }
}
