package com.astryxion.hats.client.render;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class HatLauncherClientExtensions implements IClientItemExtensions {

    public static final HatLauncherClientExtensions INSTANCE = new HatLauncherClientExtensions();

    private HatLauncherClientExtensions() {}

    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(INSTANCE);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return HatLauncherItemRenderer.getInstance();
    }
}
