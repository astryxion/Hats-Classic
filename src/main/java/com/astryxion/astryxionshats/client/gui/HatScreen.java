package com.astryxion.astryxionshats.client.gui;

import com.astryxion.astryxionshats.AstryxionsHats;
import com.astryxion.astryxionshats.Config;
import com.astryxion.astryxionshats.common.equip.HatEquipController;
import com.astryxion.astryxionshats.common.capability.HatDataCapability;
import com.astryxion.astryxionshats.common.hat.HatManager;
import com.astryxion.astryxionshats.common.hat.HatMode;
import com.astryxion.astryxionshats.common.hat.HatPart;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class HatScreen extends Screen {

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
                HatPacketHandler.sendOpenCosmeticToServer();
            } else if (Config.hatMode == HatMode.HUNTING) {
                HatPacketHandler.sendOpenHuntingToServer();
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
                            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                            if (id == null || !id.getNamespace().equals(AstryxionsHats.MODID)) return false;

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
                    String name = item.getDescription().getString().toLowerCase(Locale.ROOT);
                    boolean matchesSearch = name.contains(query);
                    String rarity = item.getRarity(new ItemStack(item)).name();
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
                HatPacketHandler.sendEquipToServer("none");
            }
        }).bounds(centerX + 92, centerY - 80, 18, 16).build());

        addRenderableWidget(Button.builder(Component.literal("?").withStyle(net.minecraft.ChatFormatting.GOLD), b -> {
            if (!unlockedHats.isEmpty() && minecraft.player != null) {
                selectedHat = unlockedHats.get(new Random().nextInt(unlockedHats.size()));
                Item hat = selectedHat;
                HatEquipController.equip(minecraft.player, hat);
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(hat);
                HatDataCapability.get(minecraft.player).ifPresent(data -> data.setEquippedHat(id));
                HatPacketHandler.sendEquipToServer(id.toString());
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
                    addRenderableWidget(Button.builder(hat.getDescription(), b -> {
                        selectedHat = hat;
                        if (minecraft.player != null) {
                            HatEquipController.equip(minecraft.player, hat);
                            ResourceLocation id = BuiltInRegistries.ITEM.getKey(hat);
                            HatDataCapability.get(minecraft.player).ifPresent(data -> data.setEquippedHat(id));
                            HatPacketHandler.sendEquipToServer(id.toString());
                        }
                    }).bounds(centerX + 5, centerY - 80 + (i * 22), 80, 20).build());
                }
            }
        }

        addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (currentPage > 0) { currentPage--; createMenuButtons(); }
        }).bounds(centerX + 5, centerY + 55, 18, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(centerX + 26, centerY + 55, 38, 20).build());

        addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if ((currentPage + 1) * HATS_PER_PAGE < filteredHats.size()) { currentPage++; createMenuButtons(); }
        }).bounds(centerX + 67, centerY + 55, 18, 20).build());
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx);
        int centerX = width / 2;
        int centerY = (height / 2) + 5;

        gfx.fill(centerX - 90, centerY - 85, centerX + 90, centerY + 80, 0xFFC6C6C6);
        gfx.renderOutline(centerX - 91, centerY - 86, 181, 167, 0xFF000000);
        gfx.fill(centerX - 85, centerY - 80, centerX + 0, centerY + 75, 0xFF000000);

        if (minecraft != null && minecraft.player != null) {
            HatPart part = HatManager.get(minecraft.player);
            ItemStack originalHat = part != null ? part.getHatStack().copy() : ItemStack.EMPTY;
            ItemStack preview = selectedHat != null ? new ItemStack(selectedHat) : ItemStack.EMPTY;

            if (part != null) {
                part.setHatStack(preview);
            }

            InventoryScreen.renderEntityInInventoryFollowsMouse(gfx, centerX - 42, centerY + 73, 55,
                    (float) (centerX - 42) - mouseX, (float) (centerY + 10) - mouseY, minecraft.player);

            if (part != null) {
                part.setHatStack(originalHat);
            }
        }

        gfx.pose().pushPose();
        gfx.pose().scale(0.9f, 0.9f, 0.9f);
        float sx = (centerX - 90) / 0.9f;
        float sy = (centerY - 95) / 0.9f;
        float sr = (centerX + 90) / 0.9f;

        String modeTag = Config.hatMode == HatMode.COSMETIC ? "[COSMETIC] " : "";
        String title = isCategoryMenu ? "Select Category" : modeTag + "Viewing: " + currentRarity + " (" + filteredHats.size() + ")";

        gfx.drawString(font, title, (int) sx, (int) sy, 0xFFFFFF);

        int pages = (int) Math.ceil((double) filteredHats.size() / HATS_PER_PAGE);
        if (!isCategoryMenu && pages > 0) {
            String pageText = "Page " + (currentPage + 1) + "/" + pages;
            gfx.drawString(font, pageText, (int) (sr - font.width(pageText)), (int) sy, 0xFFFFFF);
        }
        gfx.pose().popPose();

        super.render(gfx, mouseX, mouseY, partialTick);
    }
}
