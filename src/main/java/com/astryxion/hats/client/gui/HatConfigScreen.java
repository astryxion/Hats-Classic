package com.astryxion.hats.client.gui;

import com.astryxion.hats.Config;
import com.astryxion.hats.client.gui.widget.HatsButton;
import com.astryxion.hats.client.gui.widget.HatsCycleButton;
import com.astryxion.hats.client.gui.widget.HatsToggleButton;
import com.astryxion.hats.common.hat.GuiStyle;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.hatstand.HatStandGuiStyle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class HatConfigScreen extends Screen {

    private enum Tab {
        GENERAL,
        MOBS
    }

    private final Screen parent;
    private final GuiStyle originalGuiStyle;
    private final HatStandGuiStyle originalHatStandGuiStyle;
    private Tab tab = Tab.GENERAL;
    private ConfigList list;

    public HatConfigScreen(Screen parent) {
        super(Component.translatable("hats.config.title"));
        this.parent = parent;
        this.originalGuiStyle = Config.guiStyle;
        this.originalHatStandGuiStyle = Config.hatStandGuiStyle;
    }

    @Override
    protected void init() {
        list = new ConfigList(minecraft, width, height - 80, 48, 25);
        fillList();
        addWidget(list);

        int left = width / 2 - 155;
        HatsButton generalTab = HatsButton.create(left, 22, 150, 20, Component.translatable("hats.config.tab.general"), button -> switchTab(Tab.GENERAL));
        generalTab.active = tab != Tab.GENERAL;
        addRenderableWidget(generalTab);
        HatsButton mobsTab = HatsButton.create(left + 160, 22, 150, 20, Component.translatable("hats.config.tab.mobs"), button -> switchTab(Tab.MOBS));
        mobsTab.active = tab != Tab.MOBS;
        addRenderableWidget(mobsTab);

        addRenderableWidget(HatsButton.create(width / 2 - 100, height - 27, 200, 20, CommonComponents.GUI_DONE, button -> onClose()));
    }

    private void switchTab(Tab next) {
        tab = next;
        rebuildWidgets();
    }

    private void fillList() {
        list.clearEntries();
        if (tab == Tab.GENERAL) {
            list.addBig(new HatsCycleButton<>(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.hatMode"),
                    List.of(HatMode.HUNTING, HatMode.COSMETIC),
                    Config.hatMode,
                    mode -> Component.translatable(mode == HatMode.HUNTING ? "hats.config.hatMode.hunting" : "hats.config.hatMode.cosmetic"),
                    "hats.config.hatMode.tooltip",
                    Config::setHatMode
            ));
            list.addBig(new HatsCycleButton<>(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.guiStyle"),
                    List.of(GuiStyle.LEGACY, GuiStyle.MODERN),
                    Config.guiStyle,
                    style -> Component.translatable(style == GuiStyle.MODERN ? "hats.config.guiStyle.modern" : "hats.config.guiStyle.legacy"),
                    "hats.config.guiStyle.tooltip",
                    Config::setGuiStyle
            ));
            list.addBig(new HatsCycleButton<>(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.hatStandGuiStyle"),
                    List.of(HatStandGuiStyle.LEGACY, HatStandGuiStyle.MODERN),
                    Config.hatStandGuiStyle,
                    style -> Component.translatable(style == HatStandGuiStyle.MODERN
                            ? "hats.config.guiStyle.modern"
                            : "hats.config.guiStyle.legacy"),
                    "hats.config.hatStandGuiStyle.tooltip",
                    Config::setHatStandGuiStyle
            ));
            list.addBig(new HatsToggleButton(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.showWelcomeMessage"),
                    Config.showWelcomeMessage,
                    "hats.config.showWelcomeMessage.tooltip",
                    Config::setShowWelcomeMessage
            ));
            list.addBig(new HatsToggleButton(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.showRarityColorsEverywhere"),
                    Config.showRarityColorsEverywhere,
                    "hats.config.showRarityColorsEverywhere.tooltip",
                    Config::setShowRarityColorsEverywhere
            ));
            list.addBig(new HatsToggleButton(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.mobHatTakeover"),
                    Config.mobHatTakeover,
                    "hats.config.mobHatTakeover.tooltip",
                    Config::setMobHatTakeover
            ));
            list.addBig(new HatsToggleButton(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.bossMobHats"),
                    Config.bossMobHats,
                    "hats.config.bossMobHats.tooltip",
                    Config::setBossMobHats
            ));
            list.addBig(new HatsToggleButton(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.preventUndeadFire"),
                    Config.preventUndeadFire,
                    "hats.config.preventUndeadFire.tooltip",
                    Config::setPreventUndeadFire
            ));
            list.addBig(new HatsToggleButton(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.disableContributorHats"),
                    Config.disableContributorHats,
                    "hats.config.disableContributorHats.tooltip",
                    Config::setDisableContributorHats
            ));
            list.addBig(new HatsToggleButton(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.hatLauncherReplacesPlayerHat"),
                    Config.hatLauncherReplacesPlayerHat,
                    "hats.config.hatLauncherReplacesPlayerHat.tooltip",
                    Config::setHatLauncherReplacesPlayerHat
            ));
            list.addBig(new HatsToggleButton(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.hatLauncherKeepsInventoryHat"),
                    Config.hatLauncherDoesNotRemoveHatFromInventory,
                    "hats.config.hatLauncherKeepsInventoryHat.tooltip",
                    Config::setHatLauncherKeepsInventoryHat
            ));
            list.addBig(new HatsToggleButton(
                    width / 2 - 155, 0, 310, 20,
                    Component.translatable("hats.config.hatUnlockAnnouncement"),
                    Config.hatUnlockAnnouncement,
                    "hats.config.hatUnlockAnnouncement.tooltip",
                    Config::setHatUnlockAnnouncement
            ));
            SpawnChanceSlider slider = new SpawnChanceSlider(width / 2 - 155, 0, 310, 20);
            slider.setTooltip(Tooltip.create(Component.translatable("hats.config.mobHatSpawnChance.tooltip")));
            list.addBig(slider);
            return;
        }

        list.addSmall(
                HatsButton.create(width / 2 - 155, 0, 150, 20, Component.translatable("hats.config.mobs.allOn"), button -> {
                    Config.setAllMobHats(true);
                    double scroll = list.scrollAmount();
                    fillList();
                    list.setScrollAmount(scroll);
                }),
                HatsButton.create(width / 2 - 155 + 160, 0, 150, 20, Component.translatable("hats.config.mobs.allOff"), button -> {
                    Config.setAllMobHats(false);
                    double scroll = list.scrollAmount();
                    fillList();
                    list.setScrollAmount(scroll);
                })
        );

        List<EntityType<?>> types = Config.HAT_MOB_TYPES;
        for (int i = 0; i < types.size(); i += 2) {
            EntityType<?> left = types.get(i);
            EntityType<?> right = i + 1 < types.size() ? types.get(i + 1) : null;
            list.addSmall(mobToggle(left, width / 2 - 155), right == null ? null : mobToggle(right, width / 2 - 155 + 160));
        }
    }

    private HatsToggleButton mobToggle(EntityType<?> type, int x) {
        boolean bossLocked = Config.isBossHatMob(type) && !Config.bossMobHats;
        HatsToggleButton button = new HatsToggleButton(
                x, 0, 150, 20,
                type.getDescription(),
                Config.canMobSpawnWithHats(type),
                bossLocked ? "hats.config.mobs.bossLocked.tooltip" : "hats.config.mobs.toggle.tooltip",
                value -> {
                    if (Config.isBossHatMob(type) && !Config.bossMobHats) {
                        return;
                    }
                    Config.setMobHatEnabled(type, value);
                }
        );
        button.active = !bossLocked;
        return button;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        list.extractRenderState(gfx, mouseX, mouseY, partialTick);
        gfx.centeredText(font, title, width / 2, 8, 0xFFFFFFFF);
        super.extractRenderState(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Config.save();
        int standId = parent instanceof HatScreen legacy ? legacy.standEntityId()
                : parent instanceof ModernHatsScreen modern ? modern.standEntityId() : -1;
        if (standId >= 0 && Config.hatStandGuiStyle != originalHatStandGuiStyle) {
            minecraft.setScreen(HatStandGui.create(standId));
        } else if (standId < 0 && HatsGui.isHatMenu(parent) && Config.guiStyle != originalGuiStyle) {
            minecraft.setScreen(HatsGui.create());
        } else {
            minecraft.setScreen(parent);
        }
    }

    private static class ConfigList extends ContainerObjectSelectionList<ConfigList.Entry> {
        ConfigList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
            centerListVertically = false;
        }

        @Override
        public void clearEntries() {
            super.clearEntries();
        }

        void addBig(AbstractWidget widget) {
            addEntry(Entry.big(widget));
        }

        void addSmall(AbstractWidget left, AbstractWidget right) {
            addEntry(Entry.small(left, right));
        }

        @Override
        public int getRowWidth() {
            return 400;
        }

        @Override
        protected int scrollBarX() {
            return super.scrollBarX() + 32;
        }

        private static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
            private final List<AbstractWidget> children;

            private Entry(List<AbstractWidget> children) {
                this.children = children;
            }

            static Entry big(AbstractWidget widget) {
                return new Entry(List.of(widget));
            }

            static Entry small(AbstractWidget left, AbstractWidget right) {
                return right == null ? new Entry(List.of(left)) : new Entry(List.of(left, right));
            }

            @Override
            public void extractContent(GuiGraphicsExtractor gfx, int mouseX, int mouseY, boolean hovered, float partialTick) {
                for (AbstractWidget widget : children) {
                    widget.setY(getY());
                    widget.extractRenderState(gfx, mouseX, mouseY, partialTick);
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return children;
            }
        }
    }

    private static class SpawnChanceSlider extends AbstractSliderButton {
        SpawnChanceSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), Config.mobHatSpawnChance);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int percent = (int) Math.round(value * 100.0D);
            setMessage(Component.translatable("hats.config.mobHatSpawnChance", percent));
        }

        @Override
        protected void applyValue() {
            Config.setMobHatSpawnChance(value);
        }
    }
}
