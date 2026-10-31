package com.astryxion.hats.common.hat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.astryxion.hats.Hats;
import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;
import net.minecraft.resources.Identifier;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class HatRarityLoader {

    private static final Map<String, HatRarity> RARITIES = new HashMap<>();

    public static void load() {
        try {
            Identifier loc = Identifier.fromNamespaceAndPath(
                    Hats.MODID,
                    "rarity/hat_rarity.json"
            );

            String rarityPath = "data/" + loc.getNamespace() + "/" + loc.getPath();
            var opened = Hats.class
                    .getClassLoader()
                    .getResourceAsStream(rarityPath);
            if (opened == null) {
                var info = net.neoforged.fml.ModList.get().getModFileById(Hats.MODID);
                if (info != null) {
                    opened = info.getFile().getContents().openFile(rarityPath);
                }
            }

            if (opened == null) {
                Hats.LOGGER.warn("hat_rarity.json not found, defaulting unlisted hats to COMMON");
            } else {
                try (var resource = opened) {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> raw = new Gson().fromJson(
                        new InputStreamReader(resource),
                        type
                );

                raw.forEach((key, value) ->
                        RARITIES.put(key, HatRarity.fromString(value))
                );

                Hats.LOGGER.info("Loaded {} hat rarities", RARITIES.size());
                }
            }

        } catch (Exception e) {
            Hats.LOGGER.error("Failed to load hat_rarity.json", e);
        }

        for (String id : TabulaHats.ids()) {
            TabulaHatDefinition def = TabulaHats.get(id);
            if (def != null) {
                RARITIES.put(id, def.rarity);
            }
        }
    }

    public static HatRarity get(String hatId) {
        return RARITIES.getOrDefault(hatId, HatRarity.COMMON);
    }

    private HatRarityLoader() {}
}
