package com.astryxion.hats.common.tabula;

import com.astryxion.hats.common.hat.HatRarity;

import java.util.ArrayList;
import java.util.List;

public final class TabulaHatDefinition {

    public final String id;
    public final String tblName;
    public final String relativePath;
    public final String displayName;
    public final HatRarity rarity;
    public final Boolean cull;
    public final String accessoryFor;
    public final String accessoryParent;
    public final List<String> accessoryLayers;
    public final List<String> hideParentParts;
    public final List<TabulaHatDefinition> accessories = new ArrayList<>();

    private TabulaHatModelData modelData;
    private byte[] textureBytes;
    private boolean loadFailed;

    TabulaHatDefinition(
            String id,
            String tblName,
            String relativePath,
            String displayName,
            HatRarity rarity,
            Boolean cull,
            String accessoryFor,
            String accessoryParent,
            List<String> accessoryLayers,
            List<String> hideParentParts
    ) {
        this.id = id;
        this.tblName = tblName;
        this.relativePath = relativePath;
        this.displayName = displayName;
        this.rarity = rarity;
        this.cull = cull;
        this.accessoryFor = accessoryFor;
        this.accessoryParent = accessoryParent;
        this.accessoryLayers = List.copyOf(accessoryLayers);
        this.hideParentParts = List.copyOf(hideParentParts);
    }

    public boolean isAccessory() {
        return accessoryFor != null;
    }

    public List<TabulaHatDefinition> allAccessories() {
        List<TabulaHatDefinition> all = new ArrayList<>();
        collectAccessories(all);
        return all;
    }

    private void collectAccessories(List<TabulaHatDefinition> out) {
        for (TabulaHatDefinition accessory : accessories) {
            out.add(accessory);
            accessory.collectAccessories(out);
        }
    }

    public TabulaHatDefinition findAccessory(String id) {
        if (this.id.equals(id)) {
            return this;
        }
        for (TabulaHatDefinition accessory : accessories) {
            TabulaHatDefinition found = accessory.findAccessory(id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public synchronized TabulaHatModelData modelData() {
        ensureLoaded();
        return modelData;
    }

    public synchronized byte[] textureBytes() {
        ensureLoaded();
        return textureBytes;
    }

    private void ensureLoaded() {
        if (modelData != null || loadFailed) {
            return;
        }
        try {
            var path = TabulaHats.resolve(relativePath);
            if (path == null) {
                loadFailed = true;
                return;
            }
            modelData = TabulaTblReader.readModel(path);
            textureBytes = TabulaTblReader.readTexture(path);
        } catch (Exception e) {
            loadFailed = true;
            com.astryxion.hats.Hats.LOGGER.error("Failed to load tabula hat '{}'", relativePath, e);
        }
    }
}
