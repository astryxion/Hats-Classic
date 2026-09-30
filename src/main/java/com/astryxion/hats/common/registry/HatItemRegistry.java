package com.astryxion.hats.common.registry;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Core hat item registry
 *
 * - Auto registers all hats from models folder
 * - Auto registers every legacy Tabula .tbl as its own hat
 * - No creative tab logic
 * - Clean permanent system
 */
public final class HatItemRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Hats.MODID);

    /** All registered hats (registry objects) */
    public static final List<RegistryObject<Item>> ALL_HATS = new ArrayList<>();

    /** Names to skip (dev junk / parents / icons) */
    private static final Set<String> BLACKLIST = Set.of(
            "hatparent",
            "hatparent2",
            "simplehats_icon",
            "haticon",
            "hatpants",
            "hat_stand",
            "hat_launcher",
            "hat_launcher_second",
            "tabula_hat",
            "adv_icon_hat",
            "adv_icon_hat10",
            "adv_icon_hat25",
            "adv_icon_hat50",
            "adv_icon_hat100",
            "adv_icon_hat200",
            "adv_icon_hat300",
            "adv_icon_hat400",
            "adv_icon_hat500",
            "adv_icon_hat545",
            "adv_icon_hat_count10",
            "adv_icon_hat_count25",
            "adv_icon_hat_count50",
            "adv_icon_colourise",
            "adv_icon_personalise",
            "adv_icon_three"
    );

    private HatItemRegistry() {}

    // =============================
    // Public register entry
    // =============================

    public static void register(IEventBus bus) {
        registerAdvancementIcons();
        autoRegisterHats();
        ITEMS.register(bus);
    }

    private static void registerAdvancementIcons() {
        for (String name : List.of(
                "adv_icon_hat",
                "adv_icon_hat10",
                "adv_icon_hat25",
                "adv_icon_hat50",
                "adv_icon_hat100",
                "adv_icon_hat200",
                "adv_icon_hat300",
                "adv_icon_hat400",
                "adv_icon_hat500",
                "adv_icon_hat545",
                "adv_icon_hat_count10",
                "adv_icon_hat_count25",
                "adv_icon_hat_count50",
                "adv_icon_colourise",
                "adv_icon_personalise",
                "adv_icon_three"
        )) {
            ITEMS.register(name, () -> new Item(new Item.Properties()));
        }
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
            Set<String> reserved = new LinkedHashSet<>(BLACKLIST);

            Path modelsPath = getResourcePath(
                    "assets/" + Hats.MODID + "/models/item"
            );

            if (modelsPath == null || !Files.exists(modelsPath)) {
                Hats.LOGGER.error("Hat models folder not found!");
            } else {
                try (Stream<Path> list = Files.list(modelsPath)) {
                    list.filter(p -> p.toString().endsWith(".json"))
                            .sorted()
                            .forEach(path -> {

                                String name = path.getFileName()
                                        .toString()
                                        .replace(".json", "");

                                if (BLACKLIST.contains(name) || isTabulaItemModel(path)) {
                                    return;
                                }

                                RegistryObject<Item> item = ITEMS.register(
                                        name,
                                        () -> new HatItem(name)
                                );

                                ALL_HATS.add(item);
                                reserved.add(name);
                            });
                }
            }

            int jsonCount = ALL_HATS.size();
            for (TabulaHatDefinition def : TabulaHats.discover(reserved)) {
                RegistryObject<Item> item = ITEMS.register(
                        def.id,
                        () -> new HatItem(def.id, def.relativePath)
                );
                ALL_HATS.add(item);
            }

            Hats.LOGGER.info("Auto-registered {} hats ({} item models, {} tabula parents)",
                    ALL_HATS.size(), jsonCount, ALL_HATS.size() - jsonCount);

        } catch (Exception e) {
            throw new RuntimeException("Failed to auto register hats", e);
        }
    }

    private static boolean isTabulaItemModel(Path path) {
        try {
            String text = Files.readString(path);
            return text.contains("hats:item/tabula_hat")
                    || text.contains("minecraft:builtin/entity");
        } catch (IOException e) {
            return false;
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
            URI uri = url.toURI();
            String uriStr = uri.toString();
            int sep = uriStr.indexOf("!/");
            URI jarUri = URI.create(uriStr.substring(0, sep));
            FileSystem fs;
            try {
                fs = FileSystems.getFileSystem(jarUri);
            } catch (FileSystemNotFoundException e) {
                try {
                    fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                } catch (FileSystemAlreadyExistsException exists) {
                    fs = FileSystems.getFileSystem(jarUri);
                }
            }
            return fs.getPath(path);
        }
        return Paths.get(url.toURI());
    }
}
