package com.astryxion.hats.common.hat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.astryxion.hats.Hats;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class HatRarityLoader {

    private static final Map<String, HatRarity> RARITIES = new HashMap<>();

    public static void load() {
        try {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                    Hats.MODID,
                    "rarity/hat_rarity.json"
            );

            var resource = Hats.class
                    .getClassLoader()
                    .getResourceAsStream(
                            "data/" + loc.getNamespace() + "/" + loc.getPath()
                    );

            if (resource == null) {
                Hats.LOGGER.warn("hat_rarity.json not found, defaulting all hats to COMMON");
                return;
            }

            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> raw = new Gson().fromJson(
                    new InputStreamReader(resource),
                    type
            );

            raw.forEach((key, value) ->
                    RARITIES.put(key, HatRarity.fromString(value))
            );

            Hats.LOGGER.info("Loaded {} hat rarities", RARITIES.size());

        } catch (Exception e) {
            Hats.LOGGER.error("Failed to load hat_rarity.json", e);
        }
    }

    public static HatRarity get(String hatId) {
        return RARITIES.getOrDefault(hatId, HatRarity.COMMON);
    }

    private HatRarityLoader() {}
}
