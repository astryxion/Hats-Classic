package com.astryxion.hats.client.gui;

import com.astryxion.hats.Config;
import com.astryxion.hats.Hats;
import com.astryxion.hats.client.gui.modern.ClientFavourites;
import com.astryxion.hats.client.hat.ClientHatUnlocks;
import com.astryxion.hats.client.gui.widget.HatsButton;
import com.astryxion.hats.client.gui.widget.HatsIconButton;
import com.astryxion.hats.client.gui.widget.HatsIcons;
import com.astryxion.hats.client.gui.widget.HatsToggleButton;
import com.astryxion.hats.client.gui.widget.HatsWidgets;
import com.astryxion.hats.common.equip.HatEquipController;
import com.astryxion.hats.common.hat.HatAccessories;
import com.astryxion.hats.common.hat.HatColor;
import com.astryxion.hats.common.hat.HatGlint;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatPart;
import com.astryxion.hats.common.hatstand.HatStandEntity;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketUpdateHatStand;
import com.astryxion.hats.common.hat.HatRarity;
import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * The Hats 1.7.10-style chooser. Hat and color edits are local previews until
 * Done is pressed; closing with X or Escape restores the original stack.
 */
public class HatScreen extends Screen {

    private static final ResourceLocation CHOOSER =
            new ResourceLocation(Hats.MODID, "textures/gui/hatchooser.png");
    private static final int GUI_W = 176;
    private static final int GUI_H = 170;
    private static final int PAGE_SIZE = 6;
    private static final int ACC_PAGE_SIZE = 2;
    private static final int ACC_CARD_H = 64;

    private final Random random = new Random();
    private final int standEntityId;
    private final List<Item> visibleHats = new ArrayList<>();

    private ItemStack originalHat = ItemStack.EMPTY;
    private ItemStack stagedHat = ItemStack.EMPTY;
    private boolean originalCaptured;
    private boolean modeAwardSent;
    private boolean committed;
    private View view = View.HATS;
    private HatRarity category;
    private int page;
    private EditBox search;
    private HatsButton resetColorButton;
    private HatsIconButton tradeButton;

    private int guiLeft;
    private int guiTop;
    private int unlockGeneration;

    public HatScreen() {
        this(-1);
    }

    public HatScreen(int standEntityId) {
        super(Component.translatable("hats.gui.title"));
        this.standEntityId = standEntityId;
    }

    @Override
    protected void init() {
        LivingEntity target = previewTarget();
        if (target == null) {
            onClose();
            return;
        }

        if (!originalCaptured) {
            originalHat = HatManager.getHat(target).copy();
            stagedHat = originalHat.copy();
            originalCaptured = true;
        }
        if (!modeAwardSent) {
            modeAwardSent = true;
            HatsGui.notifyMenuOpened(standEntityId >= 0);
        }

        guiLeft = (width - GUI_W) / 2;
        guiTop = (height - GUI_H) / 2;
        unlockGeneration = com.astryxion.hats.client.hat.ClientHatUnlocks.generation();
        setupWidgets();
        previewStaged();
    }

    @Override
    public void tick() {
        super.tick();
        int generation = com.astryxion.hats.client.hat.ClientHatUnlocks.generation();
        if (generation != unlockGeneration && view != View.ACCESSORIES && view != View.COLORIZER) {
            unlockGeneration = generation;
            rebuildContentWidgets();
        }
    }

    private void setupWidgets() {
        clearWidgets();
        refreshVisibleHats();

        search = new EditBox(font, width / 2 - 65, height - 24, 150, 20,
                Component.translatable("hats.gui.search"));
        search.setHint(Component.translatable("hats.gui.search"));
        search.setValue("");
        search.setResponder(value -> {
            page = 0;
            rebuildContentWidgets();
        });
        search.visible = view != View.ACCESSORIES;
        addRenderableWidget(search);

        rebuildContentWidgets();
    }

    private void rebuildContentWidgets() {
        String query = search != null ? search.getValue() : "";
        clearWidgets();
        if (search != null) {
            search.visible = view != View.ACCESSORIES;
            addRenderableWidget(search);
        }
        refreshVisibleHats(query);

        if (view == View.COLORIZER) {
            addColorSliders();
        } else if (view == View.ACCESSORIES) {
            addAccessoryButtons();
        } else if (view == View.CATEGORIES) {
            addCategoryButtons();
        } else {
            addHatButtons();
        }

        int navY = guiTop + 139;
        HatsButton previous = addRenderableWidget(HatsButton.create(
                guiLeft + 82, navY, 20, 20, Component.literal("<"), button -> switchPage(-1)));
        previous.active = page > 0 && view != View.COLORIZER;

        addRenderableWidget(HatsButton.create(
                guiLeft + 104, navY, 44, 20, CommonComponents.GUI_DONE, button -> commitAndClose()));

        HatsButton next = addRenderableWidget(HatsButton.create(
                guiLeft + 150, navY, 20, 20, Component.literal(">"), button -> switchPage(1)));
        next.active = page + 1 < pageCount() && view != View.COLORIZER;

        addSidebarButtons();
        HatsButton close = addRenderableWidget(HatsButton.create(
                width - 22, 2, 20, 20, Component.literal("X"), button -> discardAndClose()));
        close.setTooltip(Tooltip.create(Component.translatable("hats.gui.discardChanges")));
    }

    private void addHatButtons() {
        int start = page * PAGE_SIZE;
        for (int slot = 0; slot < PAGE_SIZE && start + slot < visibleHats.size(); slot++) {
            Item item = visibleHats.get(start + slot);
            ItemStack display = new ItemStack(item);
            Component name = display.getHoverName();
            if (item instanceof HatItem hatItem
                    && (view == View.CATEGORY || Config.showRarityColorsEverywhere)) {
                int color = hatItem.getHatRarity().getColor();
                name = name.copy().withStyle(style -> style.withColor(color));
            }
            HatsButton button = addRenderableWidget(new ScrollingHatButton(
                    guiLeft + 82, guiTop + 7 + slot * 22, 88, 20, name, item,
                    pressed -> selectHat(item)));
            button.active = stagedHat.isEmpty() || stagedHat.getItem() != item;
            button.setTooltip(Tooltip.create(name));
        }
    }

    private void addCategoryButtons() {
        List<HatRarity> categories = List.of(HatRarity.values());
        addRenderableWidget(HatsButton.create(
                guiLeft + 82, guiTop + 7, 88, 20,
                Component.translatable("hats.gui.allHats"), button -> showAllHats()));
        for (int i = 0; i < categories.size(); i++) {
            HatRarity rarity = categories.get(i);
            addRenderableWidget(HatsButton.create(
                    guiLeft + 82, guiTop + 29 + i * 22, 88, 20,
                    Component.literal(titleCase(rarity.name()))
                            .withStyle(style -> style.withColor(rarity.getColor())),
                    button -> showCategory(rarity)));
        }
    }

    private void addColorSliders() {
        boolean enabled = !stagedHat.isEmpty();
        ColorSlider red = addRenderableWidget(new ColorSlider(
                guiLeft + 82, guiTop + 7, "hats.gui.red",
                HatColor.red(stagedHat), value -> setColor(value, null, null, null)));
        ColorSlider green = addRenderableWidget(new ColorSlider(
                guiLeft + 82, guiTop + 27, "hats.gui.green",
                HatColor.green(stagedHat), value -> setColor(null, value, null, null)));
        ColorSlider blue = addRenderableWidget(new ColorSlider(
                guiLeft + 82, guiTop + 47, "hats.gui.blue",
                HatColor.blue(stagedHat), value -> setColor(null, null, value, null)));
        ColorSlider alpha = addRenderableWidget(new ColorSlider(
                guiLeft + 82, guiTop + 67, "hats.gui.alpha",
                HatColor.alpha(stagedHat), value -> setColor(null, null, null, value)));
        red.active = green.active = blue.active = alpha.active = enabled;

        HatsToggleButton glint = addRenderableWidget(new HatsToggleButton(
                guiLeft + 82, guiTop + 87, 88, 20,
                Component.translatable("hats.gui.glint"),
                HatGlint.get(stagedHat),
                "hats.gui.glint.tooltip",
                value -> {
                    HatGlint.set(stagedHat, value);
                    previewStaged();
                }));
        glint.active = enabled;

        resetColorButton = addRenderableWidget(HatsButton.create(
                guiLeft + 82, guiTop + 109, 88, 20,
                Component.translatable("hats.gui.resetColor"),
                button -> {
                    HatColor.set(stagedHat, 255, 255, 255, 255);
                    previewStaged();
                    rebuildContentWidgets();
                }));
        resetColorButton.active = enabled && !HatColor.isDefault(stagedHat);
    }

    private void addAccessoryButtons() {
        TabulaHatDefinition parent = TabulaHats.from(stagedHat);
        List<TabulaHatDefinition> accessories = parent == null ? List.of() : parent.allAccessories();
        if (accessories.isEmpty()) {
            addRenderableWidget(HatsButton.create(
                    guiLeft + 82, guiTop + 7, 88, 20,
                    Component.translatable("hats.gui.noAccessories"), button -> {})).active = false;
            return;
        }
        int start = page * ACC_PAGE_SIZE;
        for (int slot = 0; slot < ACC_PAGE_SIZE && start + slot < accessories.size(); slot++) {
            TabulaHatDefinition accessory = accessories.get(start + slot);
            boolean on = HatAccessories.isShowing(stagedHat, accessory.id);
            boolean blocked = HatAccessories.isBlocked(stagedHat, parent, accessory.id);
            AccessoryCardButton button = addRenderableWidget(new AccessoryCardButton(
                    guiLeft + 82, guiTop + 7 + slot * (ACC_CARD_H + 2), 88, ACC_CARD_H,
                    accessory, accessoryPreview(parent, accessory), on, blocked,
                    pressed -> {
                        if (HatAccessories.isBlocked(stagedHat, parent, accessory.id)) {
                            return;
                        }
                        HatAccessories.toggle(stagedHat, parent, accessory.id);
                        previewStaged();
                        rebuildContentWidgets();
                    }));
            button.active = !blocked;
            button.setTooltip(Tooltip.create(Component.literal(accessory.displayName)));
        }
    }

    private ItemStack accessoryPreview(TabulaHatDefinition parent, TabulaHatDefinition accessory) {
        ItemStack preview = stagedHat.copy();
        for (TabulaHatDefinition other : parent.allAccessories()) {
            HatAccessories.setShowing(preview, parent, other.id, false);
        }
        HatAccessories.setShowing(preview, parent, accessory.id, true);
        return preview;
    }

    private static boolean hasAccessories(ItemStack stack) {
        TabulaHatDefinition def = TabulaHats.from(stack);
        return def != null && !def.allAccessories().isEmpty();
    }

    private void addSidebarButtons() {
        int x = guiLeft + 177;
        int y = guiTop;
        int step = 21;

        HatsIconButton remove = addRenderableWidget(new HatsIconButton(
                x, y, 20, 20, HatsIcons.CANCEL,
                Component.translatable("hats.gui.removeHat"), button -> removeHat()));
        remove.active = !stagedHat.isEmpty();

        HatsIconButton colorizer = addRenderableWidget(new HatsIconButton(
                x, y + step, 20, 20,
                view == View.COLORIZER ? HatsIcons.HAT : HatsIcons.COLOURISE,
                Component.translatable(view == View.COLORIZER ? "hats.gui.hatsList" : "hats.gui.colorizer"),
                button -> toggleColorizer()));
        colorizer.active = !stagedHat.isEmpty() || view == View.COLORIZER;

        HatsIconButton accessories = addRenderableWidget(new HatsIconButton(
                x, y + step * 2, 20, 20,
                view == View.ACCESSORIES ? HatsIcons.HAT : HatsIcons.SEARCH,
                Component.translatable(view == View.ACCESSORIES ? "hats.gui.hatsList" : "hats.gui.accessories"),
                button -> toggleAccessories()));
        accessories.active = hasAccessories(stagedHat) || view == View.ACCESSORIES;

        HatsIconButton randomHat = addRenderableWidget(new HatsIconButton(
                x, y + step * 3, 20, 20, HatsIcons.RANDOMISE,
                Component.translatable("hats.gui.randomHat"), button -> randomizeHat()));
        randomHat.active = !visibleHats.isEmpty() && view != View.COLORIZER && view != View.CATEGORIES && view != View.ACCESSORIES;

        addRenderableWidget(new HatsIconButton(
                x, y + step * 4, 20, 20,
                view == View.FAVOURITES ? HatsIcons.HAT : HatsIcons.FAVOURITE,
                Component.translatable(view == View.FAVOURITES
                        ? "hats.gui.hatsList"
                        : "hats.gui.favorites.tooltip"),
                button -> handleFavourites()));

        addRenderableWidget(new HatsIconButton(
                x, y + step * 5, 20, 20,
                view == View.CATEGORIES || view == View.CATEGORY ? HatsIcons.HAT : HatsIcons.CATEGORIES,
                Component.translatable(view == View.CATEGORIES || view == View.CATEGORY
                        ? "hats.gui.hatsList"
                        : "hats.gui.categories"),
                button -> toggleCategories()));

        addRenderableWidget(new HatsIconButton(
                x, y + step * 6, 20, 20, HatsIcons.PERSONALISE,
                Component.translatable("hats.gui.personalize"),
                button -> minecraft.setScreen(new HatConfigScreen(this))));

        if (standEntityId < 0) {
            this.tradeButton = addRenderableWidget(new HatsIconButton(
                    x, y + step * 7, 20, 20, HatsIcons.TRADE,
                    Component.translatable("hats.gui.trade"),
                    button -> com.astryxion.hats.client.trade.ClientTradeFlow.openTrade()));
        } else {
            this.tradeButton = null;
        }
    }

    private void selectHat(Item item) {
        if (!stagedHat.isEmpty() && stagedHat.getItem() == item) {
            rebuildContentWidgets();
            return;
        }
        stagedHat = new ItemStack(item);
        previewStaged();
        rebuildContentWidgets();
    }

    private void removeHat() {
        stagedHat = ItemStack.EMPTY;
        view = View.HATS;
        previewStaged();
        rebuildContentWidgets();
    }

    private void toggleAccessories() {
        if (view != View.ACCESSORIES && !hasAccessories(stagedHat)) {
            return;
        }
        view = view == View.ACCESSORIES ? View.HATS : View.ACCESSORIES;
        page = 0;
        rebuildContentWidgets();
    }

    private void toggleColorizer() {
        view = view == View.COLORIZER ? View.HATS : View.COLORIZER;
        page = 0;
        rebuildContentWidgets();
    }

    private void randomizeHat() {
        if (visibleHats.isEmpty()) {
            return;
        }
        Item item = visibleHats.get(random.nextInt(visibleHats.size()));
        selectHat(item);
        page = Math.max(0, visibleHats.indexOf(item) / PAGE_SIZE);
        rebuildContentWidgets();
    }

    private void handleFavourites() {
        if (hasShiftDown() && !stagedHat.isEmpty()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stagedHat.getItem());
            if (id != null) {
                ClientFavourites.toggle(id.toString());
            }
        } else {
            view = view == View.FAVOURITES ? View.HATS : View.FAVOURITES;
            category = null;
            page = 0;
        }
        rebuildContentWidgets();
    }

    private void toggleCategories() {
        if (view == View.CATEGORIES || view == View.CATEGORY) {
            view = View.HATS;
            category = null;
        } else {
            view = View.CATEGORIES;
        }
        page = 0;
        rebuildContentWidgets();
    }

    private void showCategory(HatRarity rarity) {
        category = rarity;
        view = View.CATEGORY;
        page = 0;
        rebuildContentWidgets();
    }

    private void showAllHats() {
        category = null;
        view = View.HATS;
        page = 0;
        rebuildContentWidgets();
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
    }

    private void switchPage(int direction) {
        page = Math.max(0, Math.min(pageCount() - 1, page + direction));
        rebuildContentWidgets();
    }

    private int pageCount() {
        if (view == View.COLORIZER) {
            return 1;
        }
        int size;
        if (view == View.ACCESSORIES) {
            TabulaHatDefinition parent = TabulaHats.from(stagedHat);
            size = parent == null ? 1 : Math.max(1, parent.allAccessories().size());
            return Math.max(1, (size + ACC_PAGE_SIZE - 1) / ACC_PAGE_SIZE);
        } else if (view == View.CATEGORIES) {
            size = HatRarity.values().length + 1;
        } else {
            size = visibleHats.size();
        }
        return Math.max(1, (size + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private void refreshVisibleHats() {
        refreshVisibleHats(search != null ? search.getValue() : "");
    }

    private void refreshVisibleHats(String query) {
        visibleHats.clear();
        Player player = minecraft != null ? minecraft.player : null;
        if (player == null || view == View.CATEGORIES || view == View.COLORIZER || view == View.ACCESSORIES) {
            return;
        }

        String normalized = query.toLowerCase(Locale.ROOT).trim();
        for (Item item : HatsGui.unlockedHats(player)) {
            ItemStack stack = new ItemStack(item);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (view == View.FAVOURITES && (id == null || !ClientFavourites.isFavourite(id.toString()))) {
                continue;
            }
            if (view == View.CATEGORY
                    && (!(item instanceof HatItem hatItem) || hatItem.getHatRarity() != category)) {
                continue;
            }
            if (!normalized.isEmpty()
                    && !stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(normalized)) {
                continue;
            }
            visibleHats.add(item);
        }
        visibleHats.sort(Comparator.comparing(item ->
                new ItemStack(item).getHoverName().getString(), String.CASE_INSENSITIVE_ORDER));
        page = Math.max(0, Math.min(page, pageCount() - 1));
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

    private void restoreOriginal() {
        LivingEntity target = previewTarget();
        if (target == null || !originalCaptured) {
            return;
        }
        if (originalHat.isEmpty()) {
            HatManager.clearHat(target);
        } else {
            HatManager.setHatStack(target, originalHat.copy());
        }
    }

    private void commitAndClose() {
        Player player = minecraft != null ? minecraft.player : null;
        if (player == null) {
            discardAndClose();
            return;
        }
        committed = true;
        if (standEntityId >= 0) {
            HatPacketHandler.CHANNEL.sendToServer(new PacketUpdateHatStand(standEntityId, stagedHat.copy()));
        } else if (stagedHat.isEmpty()) {
            HatEquipController.unequip(player);
        } else {
            HatEquipController.equipStack(player, stagedHat);
        }
        minecraft.setScreen(null);
    }

    private void discardAndClose() {
        restoreOriginal();
        minecraft.setScreen(null);
    }

    @Override
    public void onClose() {
        discardAndClose();
    }

    @Override
    public void removed() {
        if (!committed) {
            restoreOriginal();
        }
        super.removed();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.blit(CHOOSER, guiLeft, guiTop, 0, 0, GUI_W, GUI_H, 256, 256);

        LivingEntity target = previewTarget();
        if (target != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    guiLeft + 42, guiTop + 155,
                    55,
                    (float) (guiLeft + 42) - mouseX,
                    (float) (guiTop + 63) - mouseY,
                    target
            );
        }

        Component display = displayName();
        if (view != View.COLORIZER) {
            display = display.copy().append(" (" + (page + 1) + "/" + pageCount() + ")");
        }
        graphics.drawString(font,
                Component.translatable("hats.gui.viewing").append(": ").append(display),
                guiLeft + 1, guiTop - 10, 0xFFFFFF);

        if (search != null && search.visible) {
            graphics.blit(HatsIcons.SEARCH, search.getX() - 20, search.getY() + 2,
                    16, 16, 0, 0, 16, 16, 16, 16);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
        if (tradeButton != null && com.astryxion.hats.client.trade.ClientTradeState.hasIncomingOffers()) {
            graphics.fill(tradeButton.getX() + tradeButton.getWidth() - 5, tradeButton.getY() + 1,
                    tradeButton.getX() + tradeButton.getWidth() - 1, tradeButton.getY() + 5, 0xFFFF3333);
        }
    }

    private Component displayName() {
        if (view == View.COLORIZER) {
            return Component.translatable("hats.gui.colorizer");
        }
        if (view == View.ACCESSORIES) {
            if (stagedHat.isEmpty()) {
                return Component.translatable("hats.gui.accessories");
            }
            return Component.translatable("hats.gui.accessories")
                    .append(": ")
                    .append(stagedHat.getHoverName());
        }
        if (view == View.CATEGORIES) {
            return Component.translatable("hats.gui.categories");
        }
        if (view == View.FAVOURITES) {
            return Component.translatable("hats.gui.favorites");
        }
        if (view == View.CATEGORY && category != null) {
            return Component.literal(titleCase(category.name()));
        }
        return Component.translatable("hats.gui.allHats");
    }

    private static String titleCase(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
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

    public static LivingEntity getPreviewEntityForRendering() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.screen instanceof HatScreen screen ? screen.previewTarget() : null;
    }

    private enum View {
        HATS,
        COLORIZER,
        ACCESSORIES,
        CATEGORIES,
        FAVOURITES,
        CATEGORY
    }

    private final class AccessoryCardButton extends HatsButton {

        private final TabulaHatDefinition accessory;
        private final ItemStack preview;
        private final boolean enabled;
        private final boolean blocked;

        private AccessoryCardButton(
                int x,
                int y,
                int width,
                int height,
                TabulaHatDefinition accessory,
                ItemStack preview,
                boolean enabled,
                boolean blocked,
                OnPress onPress
        ) {
            super(x, y, width, height, Component.literal(accessory.displayName), onPress);
            this.accessory = accessory;
            this.preview = preview;
            this.enabled = enabled;
            this.blocked = blocked;
        }

        @Override
        protected HatsWidgets.ButtonState buttonState() {
            if (blocked) {
                return HatsWidgets.ButtonState.CLICK;
            }
            if (enabled) {
                return isHoveredOrFocused() ? HatsWidgets.ButtonState.HOVER : HatsWidgets.ButtonState.CLICK;
            }
            return super.buttonState();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            HatsWidgets.renderMinecraftStyleButton(graphics, getX(), getY(), getWidth(), getHeight(), buttonState());
            renderPreviewModel(graphics);
            if (enabled) {
                graphics.fill(getX() + 1, getY() + 1, getX() + 4, getY() + getHeight() - 1, 0xFF44FF44);
            }
            if (blocked) {
                graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0x88000000);
            }
            renderLabel(graphics);
        }

        private void renderPreviewModel(GuiGraphics graphics) {
            LivingEntity target = previewTarget();
            if (target == null) {
                return;
            }
            int previewBottom = getY() + getHeight() - 12;
            HatPreviewRenderer.render(graphics, target, preview.copy(),
                    getX() + getWidth() / 2, previewBottom + 54, 42,
                    getX() + 2, getY() + 2, getX() + getWidth() - 2, previewBottom);
        }

        @Override
        protected void renderLabel(GuiGraphics graphics) {
            int color = blocked ? 0xA0A0A0 : accessory.rarity.getColor();
            renderScrollingString(
                    graphics,
                    Minecraft.getInstance().font,
                    getMessage(),
                    getX() + 5,
                    getY() + getHeight() - 12,
                    getX() + getWidth() - 4,
                    getY() + getHeight() - 2,
                    color
            );
        }
    }

    private static final class ScrollingHatButton extends HatsButton {

        private final Item hat;

        private ScrollingHatButton(
                int x, int y, int width, int height, Component message, Item hat, OnPress onPress
        ) {
            super(x, y, width, height, message, onPress);
            this.hat = hat;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            int copies = ClientHatUnlocks.count(ForgeRegistries.ITEMS.getKey(hat));
            if (copies <= 1) {
                return;
            }
            var font = Minecraft.getInstance().font;
            float scale = 0.6F;
            String label = "x" + copies;
            graphics.pose().pushPose();
            graphics.pose().translate(
                    getX() + getWidth() - 2 - font.width(label) * scale,
                    getY() + getHeight() - 2 - font.lineHeight * scale,
                    300.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, label, 0, 0, 0xFFFFFFFF, true);
            graphics.pose().popPose();
        }

        @Override
        protected void renderLabel(GuiGraphics graphics) {
            int color = active ? 0xFFFFFF : 0xA0A0A0;
            renderScrollingString(
                    graphics,
                    Minecraft.getInstance().font,
                    getMessage(),
                    getX() + 4,
                    getY(),
                    getX() + getWidth() - 4,
                    getY() + getHeight(),
                    color
            );
        }
    }

    private final class ColorSlider extends AbstractSliderButton {

        private final String translationKey;
        private final java.util.function.IntConsumer consumer;

        private ColorSlider(int x, int y, String translationKey, int channel,
                java.util.function.IntConsumer consumer) {
            super(x, y, 88, 20, Component.empty(), channel / 255.0D);
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
