package com.astryxion.hats.client.render;

import com.astryxion.hats.Hats;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class HatLauncherClientExtensions implements IClientItemExtensions {

    public static final HatLauncherClientExtensions INSTANCE = new HatLauncherClientExtensions();

    private HatLauncherClientExtensions() {}

    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(INSTANCE);
    }

    @EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT)
    public static final class Models {
        private Models() {}

        @SubscribeEvent
        public static void onBake(ModelEvent.ModifyBakingResult event) {
            event.getBakingResult().itemStackModels().put(
                    Identifier.fromNamespaceAndPath(Hats.MODID, "hat_launcher"),
                    HatLauncherItemRenderer.getInstance()
            );
        }
    }
}
