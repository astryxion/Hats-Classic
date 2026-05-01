package com.astryxion.hats.common.registry;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final String ITEM_MODELS_PREFIX = "assets/" + Hats.MODID + "/models/item/";
    /** Exists in resources so we can resolve the models/item folder in dev (file) and release (jar). */
    private static final String MODELS_FOLDER_MARKER = ITEM_MODELS_PREFIX + "thumbnail.json";

    private static void autoRegisterHats() {
        try {
            URL marker = HatItemRegistry.class.getClassLoader().getResource(MODELS_FOLDER_MARKER);
            if (marker == null) {
                Hats.LOGGER.error("Hat models folder not found (missing resource {}).", MODELS_FOLDER_MARKER);
                return;
            }

            List<String> stems;
            if ("jar".equalsIgnoreCase(marker.getProtocol())) {
                JarURLConnection connection = (JarURLConnection) marker.openConnection();
                try (JarFile jar = connection.getJarFile()) {
                    stems = listJsonStemsFromJar(jar);
                }
            } else {
                Path dir = Paths.get(marker.toURI()).getParent();
                if (dir == null || !Files.isDirectory(dir)) {
                    Hats.LOGGER.error("Hat models folder not found (bad path for {}).", MODELS_FOLDER_MARKER);
                    return;
                }
                stems = listJsonStemsFromDir(dir);
            }

            for (String name : stems) {
                if (BLACKLIST.contains(name.toLowerCase(Locale.ROOT))) continue;
                Identifier id = Identifier.fromNamespaceAndPath(Hats.MODID, name);
                ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
                Item registered = Registry.register(
                        BuiltInRegistries.ITEM,
                        id,
                        new HatItem(name, new Item.Properties().stacksTo(1).setId(itemKey)));
                ALL_HATS_LIST.add(registered);
            }

            Hats.LOGGER.info("Auto-registered {} hats", ALL_HATS_LIST.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to auto register hats", e);
        }
    }

    private static List<String> listJsonStemsFromJar(JarFile jar) {
        List<String> out = new ArrayList<>();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            if (!name.startsWith(ITEM_MODELS_PREFIX) || !name.endsWith(".json")) continue;
            String stem = name.substring(ITEM_MODELS_PREFIX.length(), name.length() - ".json".length());
            if (!stem.isEmpty()) out.add(stem);
        }
        Collections.sort(out);
        return out;
    }

    private static List<String> listJsonStemsFromDir(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replace(".json", ""))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}
