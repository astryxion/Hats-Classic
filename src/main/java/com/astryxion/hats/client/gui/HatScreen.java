package com.astryxion.hats.client.gui;

import com.astryxion.hats.Hats;
import com.astryxion.hats.Config;
import com.astryxion.hats.client.equip.HatEquipController;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.hat.HatPart;
import com.astryxion.hats.common.hat.HatRarityLoader;
import com.astryxion.hats.client.network.HatClientPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hat picker UI — layout and vanilla {@link Button} styling match Forge 1.20.1;
 * registry/capability calls use NeoForge 26.1 APIs.
 */
public class HatScreen extends Screen {

    public static LivingEntity getPreviewEntityForRendering() {
        if (net.minecraft.client.Minecraft.getInstance().screen instanceof HatScreen hs
                && hs.minecraft != null
                && hs.minecraft.player != null) {
            return hs.minecraft.player;
        }
        return null;
    }

    private final List<Item> unlockedHats = new ArrayList<>();
    private List<Item> filteredHats = new ArrayList<>();

    private int currentPage = 0;
    private static final int HATS_PER_PAGE = 6;

    private Item selectedHat = null;
    private EditBox searchBox;

    private boolean isCategoryMenu = false;
    private String currentRarity = "All";

    public HatScreen() {
        super(Component.literal("Viewing: All Hats"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        searchBox = new EditBox(font, centerX - 80, height - 25, 160, 20, Component.literal("Search"));
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);

        if (minecraft != null && minecraft.player != null) {
            ItemStack equipped = HatEquipController.getEquipped(minecraft.player);
            if (!equipped.isEmpty()) {
                selectedHat = equipped.getItem();
            }

            if (Config.hatMode == HatMode.COSMETIC) {
                HatClientPackets.sendOpenCosmeticToServer();
            } else if (Config.hatMode == HatMode.HUNTING) {
                HatClientPackets.sendOpenHuntingToServer();
            }
        }

        refreshUnlockedHats();
        updateFilteredHats();
        createMenuButtons();
    }

    private void refreshUnlockedHats() {
        unlockedHats.clear();
        if (minecraft == null || minecraft.player == null) return;

        boolean isCosmeticMode = Config.hatMode == HatMode.COSMETIC;

        unlockedHats.addAll(
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> {
                            Identifier id = BuiltInRegistries.ITEM.getKey(item);
                            if (id == null || !id.getNamespace().equals(Hats.MODID)) return false;

                            String path = id.getPath().toLowerCase(Locale.ROOT);
                            if (path.contains("icon")) return false;

                            if (minecraft.player.isCreative() || isCosmeticMode) return true;

                            return HatDataCapability.get(minecraft.player)
                                    .map(data -> data.hasHat(id))
                                    .orElse(false);
                        })
                        .collect(Collectors.toList())
        );
    }

    private void onSearchChanged(String text) {
        updateFilteredHats();
        currentPage = 0;
        createMenuButtons();
    }

    private void updateFilteredHats() {
        String query = searchBox.getValue().toLowerCase(Locale.ROOT);

        filteredHats = unlockedHats.stream()
                .filter(item -> {
                    String name = new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT);
                    boolean matchesSearch = name.contains(query);
                    String rarity = HatRarityLoader.get(BuiltInRegistries.ITEM.getKey(item).getPath()).name();
                    boolean matchesRarity = currentRarity.equals("All") || rarity.equalsIgnoreCase(currentRarity);
                    return matchesSearch && matchesRarity;
                })
                .collect(Collectors.toList());
    }

    private void createMenuButtons() {
        clearWidgets();
        addRenderableWidget(searchBox);

        int centerX = width / 2;
        int centerY = (height / 2) + 5;

        addRenderableWidget(Button.builder(Component.literal("X"), b -> onClose()).bounds(width - 20, 5, 15, 15).build());

        addRenderableWidget(Button.builder(Component.literal("X").withStyle(net.minecraft.ChatFormatting.RED), b -> {
            selectedHat = null;
            HatEquipController.unequip(minecraft.player);
            if (minecraft.player != null) {
                HatDataCapability.get(minecraft.player).ifPresent(data -> data.setEquippedHat(null));
                HatClientPackets.sendEquipToServer("none");
            }
        }).bounds(centerX + 92, centerY - 80, 18, 16).build());

        addRenderableWidget(Button.builder(Component.literal("?").withStyle(net.minecraft.ChatFormatting.GOLD), b -> {
            if (!unlockedHats.isEmpty() && minecraft.player != null) {
                selectedHat = unlockedHats.get(new Random().nextInt(unlockedHats.size()));
                Item hat = selectedHat;
                HatEquipController.equip(minecraft.player, hat);
                Identifier id = BuiltInRegistries.ITEM.getKey(hat);
                HatDataCapability.get(minecraft.player).ifPresent(data -> data.setEquippedHat(id));
                HatClientPackets.sendEquipToServer(id.toString());
            }
        }).bounds(centerX + 92, centerY - 62, 18, 16).build());

        addRenderableWidget(Button.builder(Component.literal("C"), b -> {
            isCategoryMenu = !isCategoryMenu;
            createMenuButtons();
        }).bounds(centerX + 92, centerY - 44, 18, 16).build());

        if (isCategoryMenu) {
            String[] rarities = {"All", "EPIC", "RARE", "UNCOMMON", "COMMON"};
            for (int i = 0; i < rarities.length; i++) {
                String rarityName = rarities[i];
                addRenderableWidget(Button.builder(Component.literal(rarityName), b -> {
                    currentRarity = rarityName;
                    isCategoryMenu = false;
                    currentPage = 0;
                    updateFilteredHats();
                    createMenuButtons();
                }).bounds(centerX + 5, centerY - 80 + (i * 22), 80, 20).build());
            }
        } else {
            for (int i = 0; i < HATS_PER_PAGE; i++) {
                int index = (currentPage * HATS_PER_PAGE) + i;
                if (index < filteredHats.size()) {
                    Item hat = filteredHats.get(index);
                    addRenderableWidget(Button.builder(new ItemStack(hat).getHoverName(), b -> {
                        selectedHat = hat;
                        if (minecraft.player != null) {
                            HatEquipController.equip(minecraft.player, hat);
                            Identifier id = BuiltInRegistries.ITEM.getKey(hat);
                            HatDataCapability.get(minecraft.player).ifPresent(data -> data.setEquippedHat(id));
                            HatClientPackets.sendEquipToServer(id.toString());
                        }
                    }).bounds(centerX + 5, centerY - 80 + (i * 22), 80, 20).build());
                }
            }
        }

        addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (currentPage > 0) {
                currentPage--;
                createMenuButtons();
            }
        }).bounds(centerX + 5, centerY + 55, 18, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(centerX + 26, centerY + 55, 38, 20).build());

        addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if ((currentPage + 1) * HATS_PER_PAGE < filteredHats.size()) {
                currentPage++;
                createMenuButtons();
            }
        }).bounds(centerX + 67, centerY + 55, 18, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int centerY = (height / 2) + 5;

        graphics.fill(centerX - 90, centerY - 85, centerX + 90, centerY + 80, 0xFFC6C6C6);
        int ox = centerX - 91;
        int oy = centerY - 86;
        int ow = 181;
        int oh = 167;
        int outline = 0xFF000000;
        graphics.fill(ox, oy, ox + ow, oy + 1, outline);
        graphics.fill(ox, oy + oh - 1, ox + ow, oy + oh, outline);
        graphics.fill(ox, oy, ox + 1, oy + oh, outline);
        graphics.fill(ox + ow - 1, oy, ox + ow, oy + oh, outline);
        final int previewTop = centerY - 80;
        final int previewBottom = centerY + 75;
        graphics.fill(centerX - 85, previewTop, centerX + 0, previewBottom, 0xFF000000);

        if (minecraft != null && minecraft.player != null) {
            HatPart part = HatManager.get(minecraft.player);
            ItemStack originalHat = part != null ? part.getHatStack().copy() : ItemStack.EMPTY;
            ItemStack preview = selectedHat != null ? new ItemStack(selectedHat) : ItemStack.EMPTY;

            if (part != null) {
                part.setHatStack(preview);
            }

            LivingEntity pl = minecraft.player;
            int x0 = centerX - 85;
            int x1 = centerX;
            // Move the figure up inside the same black box: shift the layout window up slightly and
            // use a modest scale so feet stay inside the scissor (smaller than 55 avoids heavy clipping).
            final int previewEntityShiftUp = 14;
            int y0 = previewTop - previewEntityShiftUp;
            int y1 = previewBottom - previewEntityShiftUp;
            float centerBoxX = (x0 + x1) / 2.0F;
            float centerBoxY = (y0 + y1) / 2.0F;
            float xAngle = (float) Math.atan((centerBoxX - mouseX) / 40.0F);
            float yAngle = (float) Math.atan((centerBoxY - mouseY) / 40.0F);

            graphics.enableScissor(x0, previewTop, x1, previewBottom);
            InventoryScreen.renderEntityInInventoryFollowsAngle(
                    graphics,
                    x0,
                    y0,
                    x1,
                    y1,
                    40,
                    1.0F,
                    xAngle,
                    yAngle,
                    pl
            );
            graphics.disableScissor();

            if (part != null) {
                part.setHatStack(originalHat);
            }
        }

        int titleY = centerY - 95;
        String modeTag = Config.hatMode == HatMode.COSMETIC ? "[COSMETIC] " : "";
        String title = isCategoryMenu ? "Select Category" : modeTag + "Viewing: " + currentRarity + " (" + filteredHats.size() + ")";
        graphics.text(font, Component.literal(title), centerX - 90, titleY, 0xFFFFFF);

        int pages = (int) Math.ceil((double) filteredHats.size() / HATS_PER_PAGE);
        if (!isCategoryMenu && pages > 0) {
            String pageText = "Page " + (currentPage + 1) + "/" + pages;
            graphics.text(font, Component.literal(pageText), centerX + 90 - font.width(pageText), titleY, 0xFFFFFF);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
