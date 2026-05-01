package com.astryxion.hats;

import com.astryxion.hats.common.hat.HatMode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Clean config for Astryxion's Hats (Fabric – file-based)
 */
public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /* =========================
       Cached values
       ========================= */

    public static boolean enableHats = true;
    public static boolean enableMobHats = true;
    public static boolean enablePlayerHats = true;
    public static double mobHatSpawnChance = 0.6D;
    public static HatMode hatMode = HatMode.HUNTING;

    /* =========================
       File path
       ========================= */

    private static Path getConfigPath() {
        return net.fabricmc.loader.api.FabricLoader.getInstance()
                .getConfigDir()
                .resolve(AstryxionsHats.MODID + ".json");
    }

    /* =========================
       Load / Save
       ========================= */

    public static void load() {
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            writeDefaults(path);
            return;
        }
        try {
            String content = Files.readString(path);
            JsonObject root = GSON.fromJson(content, JsonObject.class);
            if (root == null) return;
            enableHats = root.has("enableHats") ? root.get("enableHats").getAsBoolean() : true;
            enableMobHats = root.has("enableMobHats") ? root.get("enableMobHats").getAsBoolean() : true;
            enablePlayerHats = root.has("enablePlayerHats") ? root.get("enablePlayerHats").getAsBoolean() : true;
            mobHatSpawnChance = root.has("mobHatSpawnChance") ? root.get("mobHatSpawnChance").getAsDouble() : 0.6D;
            mobHatSpawnChance = Math.max(0.0D, Math.min(1.0D, mobHatSpawnChance));
            if (root.has("hatMode")) {
                try {
                    hatMode = HatMode.valueOf(root.get("hatMode").getAsString().toUpperCase());
                } catch (Exception e) {
                    hatMode = HatMode.HUNTING;
                }
            } else {
                hatMode = HatMode.HUNTING;
            }
        } catch (Exception e) {
            AstryxionsHats.LOGGER.error("Failed to load config, using defaults", e);
        }
    }

    public static void save() {
        writeDefaults(getConfigPath());
    }

    private static void writeDefaults(Path path) {
        JsonObject root = new JsonObject();
        root.addProperty("enableHats", enableHats);
        root.addProperty("enableMobHats", enableMobHats);
        root.addProperty("enablePlayerHats", enablePlayerHats);
        root.addProperty("mobHatSpawnChance", mobHatSpawnChance);
        root.addProperty("hatMode", hatMode.name());
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root));
        } catch (IOException e) {
            AstryxionsHats.LOGGER.error("Failed to write config", e);
        }
    }

    public static void reload() {
        load();
    }
}
