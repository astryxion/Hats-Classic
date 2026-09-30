package com.astryxion.hats.client.tabula;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class TabulaHatClientExtensions implements IClientItemExtensions {

    public static final TabulaHatClientExtensions INSTANCE = new TabulaHatClientExtensions();

    private TabulaHatClientExtensions() {}

    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(INSTANCE);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return TabulaHatItemRenderer.getInstance();
    }
}
