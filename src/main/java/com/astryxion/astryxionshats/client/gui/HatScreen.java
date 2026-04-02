package com.astryxion.astryxionshats.client.gui;

import com.astryxion.astryxionshats.AstryxionsHats;
import com.astryxion.astryxionshats.Config;
import com.astryxion.astryxionshats.common.equip.HatEquipController;
import com.astryxion.astryxionshats.common.capability.HatDataCapability;
import com.astryxion.astryxionshats.common.hat.HatManager;
import com.astryxion.astryxionshats.common.hat.HatMode;
import com.astryxion.astryxionshats.common.hat.HatPart;
import com.astryxion.astryxionshats.common.hat.HatRarityLoader;
import com.astryxion.astryxionshats.common.network.HatPacketHandler;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    private RemotePlayer previewPlayer;

    /** Set during entity preview render so name-tag mixin can hide it. */
    private static LivingEntity previewEntityForRendering;

    public static LivingEntity getPreviewEntityForRendering() {
        return previewEntityForRendering;
    }

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

        if (minecraft != null && minecraft.player != null && minecraft.level != null) {
            ClientLevel level = (ClientLevel) minecraft.level;
            previewPlayer = new RemotePlayer(level, new com.mojang.authlib.GameProfile(
                    minecraft.player.getUUID(),
                    ""   // empty name = no nameplate
            )) {
                @Override
                public boolean shouldShowName() {
                    return false;
                }
            };
            previewPlayer.setCustomName(Component.empty());
            previewPlayer.setCustomNameVisible(false);
            previewPlayer.setYRot(180.0F);
            previewPlayer.setXRot(0.0F);
            previewPlayer.yHeadRot = 180.0F;
            previewPlayer.yBodyRot = 180.0F;
        }
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

        addRenderableWidget(new StyledButton(width - 20, 5, 15, 15, Component.literal("X"), b -> onClose()));

        addRenderableWidget(new StyledButton(centerX + 92, centerY - 80, 18, 16, Component.literal("X").withStyle(net.minecraft.ChatFormatting.RED), b -> {
            selectedHat = null;
            HatEquipController.unequip(minecraft.player);
            if (minecraft.player != null) {
                HatDataCapability.get(minecraft.player).ifPresent(data -> data.setEquippedHat(null));
                HatPacketHandler.sendEquipToServer("none");
            }
        }));

        addRenderableWidget(new StyledButton(centerX + 92, centerY - 62, 18, 16, Component.literal("?").withStyle(net.minecraft.ChatFormatting.GOLD), b -> {
            if (!unlockedHats.isEmpty() && minecraft.player != null) {
                selectedHat = unlockedHats.get(new Random().nextInt(unlockedHats.size()));
                Item hat = selectedHat;
                HatEquipController.equip(minecraft.player, hat);
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(hat);
                HatDataCapability.get(minecraft.player).ifPresent(data -> data.setEquippedHat(id));
                HatPacketHandler.sendEquipToServer(id.toString());
            }
        }));

        addRenderableWidget(new StyledButton(centerX + 92, centerY - 44, 18, 16, Component.literal("C"), b -> {
            isCategoryMenu = !isCategoryMenu;
            createMenuButtons();
        }));

        if (isCategoryMenu) {
            String[] rarities = {"All", "EPIC", "RARE", "UNCOMMON", "COMMON"};
            for (int i = 0; i < rarities.length; i++) {
                String rarityName = rarities[i];
                addRenderableWidget(new StyledButton(centerX + 5, centerY - 80 + (i * 23), 80, 20, Component.literal(rarityName), b -> {
                    currentRarity = rarityName;
                    isCategoryMenu = false;
                    currentPage = 0;
                    updateFilteredHats();
                    createMenuButtons();
                }));
            }
        } else {
            for (int i = 0; i < HATS_PER_PAGE; i++) {
                int index = (currentPage * HATS_PER_PAGE) + i;
                if (index < filteredHats.size()) {
                    Item hat = filteredHats.get(index);
                    addRenderableWidget(new StyledButton(centerX + 5, centerY - 80 + (i * 23), 80, 20, hat.getDescription(), b -> {
                        selectedHat = hat;
                        if (minecraft.player != null) {
                            HatEquipController.equip(minecraft.player, hat);
                            ResourceLocation id = BuiltInRegistries.ITEM.getKey(hat);
                            HatDataCapability.get(minecraft.player).ifPresent(data -> data.setEquippedHat(id));
                            HatPacketHandler.sendEquipToServer(id.toString());
                        }
                    }));
                }
            }
        }

        addRenderableWidget(new StyledButton(centerX + 5, centerY + 56, 18, 20, Component.literal("<"), b -> {
            if (currentPage > 0) { currentPage--; createMenuButtons(); }
        }));

        addRenderableWidget(new StyledButton(centerX + 26, centerY + 56, 38, 20, Component.literal("Done"), b -> onClose()));

        addRenderableWidget(new StyledButton(centerX + 67, centerY + 56, 18, 20, Component.literal(">"), b -> {
            if ((currentPage + 1) * HATS_PER_PAGE < filteredHats.size()) { currentPage++; createMenuButtons(); }
        }));
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(gfx);

        super.render(gfx, mouseX, mouseY, partialTick);

        int centerX = width / 2;
        int centerY = (height / 2) + 5;

        int gray = 0xFFC6C6C6;
        int black = 0xFF000000;

        // Left side: gray strip + black preview box (no change)
        gfx.fill(centerX - 90, centerY - 85, centerX - 85, centerY + 80, gray);
        gfx.fill(centerX - 85, centerY - 85, centerX, centerY - 80, gray);
        gfx.fill(centerX - 85, centerY - 80, centerX, centerY + 75, black);
        gfx.fill(centerX - 85, centerY + 75, centerX, centerY + 80, gray);

        // Right side: gray only where buttons are NOT (so StyledButtons stay visible)
        gfx.fill(centerX, centerY - 85, centerX + 5, centerY + 80, gray);
        gfx.fill(centerX + 5, centerY - 85, centerX + 85, centerY - 80, gray);
        gfx.fill(centerX + 5, centerY + 55, centerX + 85, centerY + 56, gray);
        gfx.fill(centerX + 5, centerY + 76, centerX + 85, centerY + 80, gray);
        gfx.fill(centerX + 85, centerY - 85, centerX + 90, centerY + 80, gray);

        gfx.renderOutline(centerX - 91, centerY - 86, 181, 167, black);

        // Entity preview (true static preview entity)
        if (minecraft != null && minecraft.player != null && previewPlayer != null) {
            // Copy real player equipment into preview
            for (var slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                previewPlayer.setItemSlot(slot, minecraft.player.getItemBySlot(slot).copy());
            }

            HatPart part = HatManager.get(previewPlayer);
            ItemStack originalHat = part != null ? part.getHatStack().copy() : ItemStack.EMPTY;
            ItemStack preview = selectedHat != null ? new ItemStack(selectedHat) : ItemStack.EMPTY;

            if (part != null) part.setHatStack(preview);

            previewPlayer.setPose(Pose.STANDING);
            previewPlayer.setCustomNameVisible(false);
            previewPlayer.setYRot(180.0F);
            previewPlayer.setXRot(0.0F);
            previewPlayer.yHeadRot = 180.0F;
            previewPlayer.yHeadRotO = 180.0F;
            previewPlayer.yBodyRot = 180.0F;
            previewPlayer.yBodyRotO = 180.0F;

            Lighting.setupForEntityInInventory();

            float px = (centerX - 85 + centerX) / 2f;
            float py = (centerY - 80 + centerY + 75) / 2f + 70f;

            previewEntityForRendering = previewPlayer;
            try {
                InventoryScreen.renderEntityInInventory(
                        gfx,
                        px,
                        py,
                        55f,
                        new Vector3f(0f, 0f, 0f),
                        new Quaternionf().rotateY((float) Math.toRadians(180.0)).rotateX((float) Math.PI),
                        new Quaternionf(),
                        previewPlayer
                );
            } finally {
                previewEntityForRendering = null;
            }

            Lighting.setupForFlatItems();

            if (part != null) part.setHatStack(originalHat);
        }

        // Title text on top
        gfx.pose().pushPose();
        gfx.pose().scale(0.9f, 0.9f, 0.9f);

        float sx = (centerX - 90) / 0.9f;
        float sy = (centerY - 95) / 0.9f;
        float sr = (centerX + 90) / 0.9f;

        String modeTag = Config.hatMode == HatMode.COSMETIC ? "[COSMETIC] " : "";
        String title = isCategoryMenu
                ? "Select Category"
                : modeTag + "Viewing: " + currentRarity + " (" + filteredHats.size() + ")";

        gfx.drawString(font, title, (int) sx, (int) sy, 0xFFFFFF);

        int pages = (int) Math.ceil((double) filteredHats.size() / HATS_PER_PAGE);
        if (!isCategoryMenu && pages > 0) {
            String pageText = "Page " + (currentPage + 1) + "/" + pages;
            gfx.drawString(font, pageText,
                    (int) (sr - font.width(pageText)),
                    (int) sy,
                    0xFFFFFF);
        }

        gfx.pose().popPose();
    }
}
