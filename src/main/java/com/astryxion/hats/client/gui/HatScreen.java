package com.astryxion.hats.client.gui;

import com.astryxion.hats.Hats;
import com.astryxion.hats.Config;
import com.astryxion.hats.common.equip.HatEquipController;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.hat.HatManager;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.hat.HatPart;
import com.astryxion.hats.common.hat.HatRarityLoader;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hat picker UI — layout and vanilla {@link Button} styling match Forge 1.20.1;
 * registry/capability calls use NeoForge 1.21.1 APIs.
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
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        // One background pass only — Screen.render() would call renderBackground() again (blur/menu),
        // which stacks the blur and leaves depth/render state wrong on 1.21.x (soft / muddy GUI).
        this.renderBackground(gfx, mouseX, mouseY, partialTick);

        int centerX = width / 2;
        int centerY = (height / 2) + 5;

        RenderSystem.enableDepthTest();

        gfx.fill(centerX - 90, centerY - 85, centerX + 90, centerY + 80, 0xFFC6C6C6);
        gfx.renderOutline(centerX - 91, centerY - 86, 181, 167, 0xFF000000);
        // Black preview column: same bounds as Forge 1.20.1 HatScreen (bottom centerY + 75).
        final int previewBottom = centerY + 75;
        gfx.fill(centerX - 85, centerY - 80, centerX + 0, previewBottom, 0xFF000000);

        if (minecraft != null && minecraft.player != null) {
            HatPart part = HatManager.get(minecraft.player);
            ItemStack originalHat = part != null ? part.getHatStack().copy() : ItemStack.EMPTY;
            ItemStack preview = selectedHat != null ? new ItemStack(selectedHat) : ItemStack.EMPTY;

            if (part != null) {
                part.setHatStack(preview);
            }

            // Point anchor + renderEntityInInventory (1.21 rect helper centers differently than 1.20).
            // No Y offset in Vector3f here — bbHeight/2 was pushing the model down and clipping the legs.
            LivingEntity pl = minecraft.player;
            float lookCx = (centerX - 85f + centerX) / 2f;
            float lookCy = (centerY - 80f + previewBottom) / 2f;
            float targetYaw = (float) Math.atan((lookCx - mouseX) / 40.0);
            float targetPitch = (float) Math.atan((lookCy - mouseY) / 40.0);

            Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf cameraPitch = new Quaternionf().rotateX(targetPitch * 20.0F * ((float) Math.PI / 180.0F));
            pose.mul(cameraPitch);

            float oldBody = pl.yBodyRot;
            float oldYaw = pl.getYRot();
            float oldPitch = pl.getXRot();
            float oldHeadO = pl.yHeadRotO;
            float oldHead = pl.yHeadRot;

            pl.yBodyRot = 180.0F + targetYaw * 20.0F;
            pl.setYRot(180.0F + targetYaw * 40.0F);
            pl.setXRot(-targetPitch * 20.0F);
            pl.yHeadRot = pl.getYRot();
            pl.yHeadRotO = pl.getYRot();

            gfx.enableScissor(centerX - 85, centerY - 80, centerX, previewBottom);
            // Between the old +58 (too high in frame) and +73 (1.20.1); feet sit a bit above the black bottom.
            final float previewAnchorY = centerY + 69f;
            InventoryScreen.renderEntityInInventory(
                    gfx,
                    centerX - 42f,
                    previewAnchorY,
                    55f,
                    new Vector3f(0f, 0f, 0f),
                    pose,
                    cameraPitch,
                    pl
            );
            gfx.disableScissor();

            pl.yBodyRot = oldBody;
            pl.setYRot(oldYaw);
            pl.setXRot(oldPitch);
            pl.yHeadRotO = oldHeadO;
            pl.yHeadRot = oldHead;

            if (part != null) {
                part.setHatStack(originalHat);
            }
        }

        // Inventory entity rendering switches to 3D lighting; restore flat GUI + flush batches
        // so fills and font draw at full pixel sharpness like vanilla widgets.
        Lighting.setupForFlatItems();
        gfx.flush();

        int titleY = centerY - 95;
        String modeTag = Config.hatMode == HatMode.COSMETIC ? "[COSMETIC] " : "";
        String title = isCategoryMenu ? "Select Category" : modeTag + "Viewing: " + currentRarity + " (" + filteredHats.size() + ")";
        gfx.drawString(font, title, centerX - 90, titleY, 0xFFFFFF);

        int pages = (int) Math.ceil((double) filteredHats.size() / HATS_PER_PAGE);
        if (!isCategoryMenu && pages > 0) {
            String pageText = "Page " + (currentPage + 1) + "/" + pages;
            gfx.drawString(font, pageText, centerX + 90 - font.width(pageText), titleY, 0xFFFFFF);
        }

        // Same as Screen.render after background — do not call super.render() (would re-run renderBackground).
        for (Renderable renderable : this.renderables) {
            renderable.render(gfx, mouseX, mouseY, partialTick);
        }
    }
}
