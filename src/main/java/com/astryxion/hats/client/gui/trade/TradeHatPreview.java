package com.astryxion.hats.client.gui.trade;

import com.astryxion.hats.client.gui.HatPreviewRenderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class TradeHatPreview {

    private TradeHatPreview() {}

    public static void render(GuiGraphics graphics, Player player, Item hat, int cellX, int cellY, int cellW, int cellH) {
        HatPreviewRenderer.render(graphics, player, hat, cellX, cellY, cellW, cellH);
    }
}
