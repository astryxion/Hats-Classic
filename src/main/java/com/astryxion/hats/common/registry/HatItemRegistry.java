package com.astryxion.hats.common.registry;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

/**
 * Core hat item registry (NeoForge)
 * Auto registers all hats from models folder
 */
public final class HatItemRegistry {

    private static final Set<String> BLACKLIST = Set.of(
            "hatparent",
            "hatparent2",
            "simplehats_icon",
            "haticon"
    );

    private static final List<DeferredItem<Item>> ALL_HATS_REGISTRY = new ArrayList<>();
    private static final List<Item> ALL_HATS_LIST = new ArrayList<>();

    public static List<Item> getAllHats() {
        if (ALL_HATS_LIST.isEmpty() && !ALL_HATS_REGISTRY.isEmpty()) {
            for (DeferredItem<Item> ro : ALL_HATS_REGISTRY) {
                ALL_HATS_LIST.add(ro.get());
            }
        }
        return new ArrayList<>(ALL_HATS_LIST);
    }

    public static List<Item> getRawAllHatsList() {
        getAllHats();
        return ALL_HATS_LIST;
    }

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Hats.MODID);

    private HatItemRegistry() {}

    public static void register(IEventBus modEventBus) {
        autoRegisterHats();
        ITEMS.register(modEventBus);
    }

    private static void autoRegisterHats() {
        try {
            Path modelsPath = getResourcePath(
                    "assets/" + Hats.MODID + "/models/item"
            );

            if (modelsPath == null || !Files.exists(modelsPath)) {
                Hats.LOGGER.error("Hat models folder not found!");
                return;
            }

            Files.list(modelsPath)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> {
                        String name = path.getFileName()
                                .toString()
                                .replace(".json", "");

                        if (BLACKLIST.contains(name)) return;

                        DeferredItem<Item> reg = ITEMS.register(name, () -> new HatItem(name));
                        ALL_HATS_REGISTRY.add(reg);
                    });

            Hats.LOGGER.info("Auto-registered {} hats", ALL_HATS_REGISTRY.size());

        } catch (Exception e) {
            throw new RuntimeException("Failed to auto register hats", e);
        }
    }

    private static Path getResourcePath(String path)
            throws IOException, URISyntaxException {

        var url = HatItemRegistry.class
                .getClassLoader()
                .getResource(path);

        if (url == null) return null;

        if (url.getProtocol().equals("jar")) {
            FileSystem fs = FileSystems.newFileSystem(
                    url.toURI(),
                    Collections.emptyMap()
            );
            return fs.getPath(path);
        } else {
            return Paths.get(url.toURI());
        }
    }
}
