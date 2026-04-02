package com.astryxion.astryxionshats.common.registry;

import com.astryxion.astryxionshats.AstryxionsHats;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

/**
 * Core hat item registry
 *
 * - Auto registers all hats from models folder
 * - No creative tab logic
 * - Clean permanent system
 */
public final class HatItemRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AstryxionsHats.MODID);

    /** All registered hats (registry objects) */
    public static final List<RegistryObject<Item>> ALL_HATS = new ArrayList<>();

    /** Names to skip (dev junk / parents / icons) */
    private static final Set<String> BLACKLIST = Set.of(
            "hatparent",
            "hatparent2",
            "simplehats_icon",
            "haticon"
    );

    private HatItemRegistry() {}

    // =============================
    // Public register entry
    // =============================

    public static void register(IEventBus bus) {
        autoRegisterHats();
        ITEMS.register(bus);
    }

    // =============================
    // Expose real Item list (FOR SPAWNING, RANDOMIZER, ETC)
    // =============================

    public static List<Item> getAllHats() {

        List<Item> hats = new ArrayList<>();

        for (RegistryObject<Item> reg : ALL_HATS) {
            hats.add(reg.get());
        }

        return hats;
    }

    // =============================
    // Auto scan model files
    // =============================

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

                        if (BLACKLIST.contains(name)) {
                            return;
                        }

                        RegistryObject<Item> item = ITEMS.register(
                                name,
                                () -> new Item(new Item.Properties())
                        );

                        ALL_HATS.add(item);
                    });

            AstryxionsHats.LOGGER.info("Auto-registered {} hats", ALL_HATS.size());

        } catch (Exception e) {
            throw new RuntimeException("Failed to auto register hats", e);
        }
    }

    // =============================
    // Resource folder access
    // =============================

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
