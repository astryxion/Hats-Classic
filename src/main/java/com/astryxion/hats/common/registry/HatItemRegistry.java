package com.astryxion.hats.common.registry;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

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
                DeferredItem<Item> reg = ITEMS.registerItem(
                        name,
                        props -> new HatItem(name, props),
                        p -> p.stacksTo(1));
                ALL_HATS_REGISTRY.add(reg);
            }

            Hats.LOGGER.info("Auto-registered {} hats", ALL_HATS_REGISTRY.size());
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
