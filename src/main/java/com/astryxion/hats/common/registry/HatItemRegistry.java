package com.astryxion.hats.common.registry;

import com.astryxion.hats.AstryxionsHats;
import com.astryxion.hats.common.hat.HatItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

/**
 * Core hat item registry (Fabric)
 * Auto registers all hats from models folder
 */
public final class HatItemRegistry {

    private static final Set<String> BLACKLIST = Set.of(
            "hatparent",
            "hatparent2",
            "simplehats_icon",
            "haticon"
    );

    private static final List<Item> ALL_HATS_LIST = new ArrayList<>();

    public static List<Item> getAllHats() {
        return new ArrayList<>(ALL_HATS_LIST);
    }

    public static List<Item> getRawAllHatsList() {
        return ALL_HATS_LIST;
    }

    private HatItemRegistry() {}

    public static void register() {
        autoRegisterHats();
    }

    private static void autoRegisterHats() {
        try {
            Path modelsPath = getResourcePath(
                    "assets/" + AstryxionsHats.MODID + "/models/item"
            );

            if (modelsPath == null || !Files.exists(modelsPath)) {
                AstryxionsHats.LOGGER.error("Hat models folder not found!");
                return;
            }

            Files.list(modelsPath)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> {
                        String name = path.getFileName()
                                .toString()
                                .replace(".json", "");

                        if (BLACKLIST.contains(name)) return;

                        ResourceLocation id = new ResourceLocation(AstryxionsHats.MODID, name);
                        HatItem item = new HatItem(name);
                        Registry.register(BuiltInRegistries.ITEM, id, item);
                        ALL_HATS_LIST.add(item);
                    });

            AstryxionsHats.LOGGER.info("Auto-registered {} hats", ALL_HATS_LIST.size());

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
