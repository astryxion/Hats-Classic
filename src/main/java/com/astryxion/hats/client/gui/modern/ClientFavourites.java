package com.astryxion.hats.client.gui.modern;

import com.astryxion.hats.Hats;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ClientFavourites {

    private static final Set<String> FAVS = new LinkedHashSet<>();
    private static boolean loaded;

    private ClientFavourites() {}

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("hats").resolve("favourites.txt");
    }

    public static void load() {
        FAVS.clear();
        loaded = true;
        Path path = file();
        if (!Files.exists(path)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    FAVS.add(trimmed);
                }
            }
        } catch (IOException e) {
            Hats.LOGGER.warn("Failed to load hat favourites: {}", e.getMessage());
        }
    }

    public static boolean isFavourite(String id) {
        if (!loaded) {
            load();
        }
        return FAVS.contains(id);
    }

    public static void toggle(String id) {
        if (!loaded) {
            load();
        }
        if (!FAVS.remove(id)) {
            FAVS.add(id);
        }
        save();
    }

    private static void save() {
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), String.join("\n", FAVS));
        } catch (IOException e) {
            Hats.LOGGER.warn("Failed to save hat favourites: {}", e.getMessage());
        }
    }
}
