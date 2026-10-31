package com.astryxion.hats.client.tabula;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.tabula.TabulaHats;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class TabulaHatClientExtensions implements IClientItemExtensions {

    public static final TabulaHatClientExtensions INSTANCE = new TabulaHatClientExtensions();

    private TabulaHatClientExtensions() {}

    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(INSTANCE);
    }

    @EventBusSubscriber(modid = Hats.MODID, value = Dist.CLIENT)
    public static final class Models {
        private Models() {}

        @SubscribeEvent
        public static void onBake(ModelEvent.ModifyBakingResult event) {
            ItemModel model = TabulaHatItemRenderer.getInstance();
            for (String id : TabulaHats.ids()) {
                event.getBakingResult().itemStackModels().put(Identifier.fromNamespaceAndPath(Hats.MODID, id), model);
            }
        }
    }
}
