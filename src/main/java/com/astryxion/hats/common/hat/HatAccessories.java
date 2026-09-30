package com.astryxion.hats.common.hat;

import com.astryxion.hats.common.tabula.TabulaHatDefinition;
import com.astryxion.hats.common.tabula.TabulaHats;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Which Tabula accessories are showing on an equipped parent hat.
 */
public final class HatAccessories {

    public static final String TAG = "HatsAccessories";

    private HatAccessories() {}

    public static List<String> showing(ItemStack stack) {
        List<String> ids = new ArrayList<>();
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return ids;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG, Tag.TAG_LIST)) {
            return ids;
        }
        ListTag list = tag.getList(TAG, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            String id = list.getString(i);
            if (!id.isEmpty()) {
                ids.add(id);
            }
        }
        return ids;
    }

    public static boolean isShowing(ItemStack stack, String accessoryId) {
        return showing(stack).contains(accessoryId);
    }

    public static void setShowing(ItemStack stack, TabulaHatDefinition parent, String accessoryId, boolean show) {
        if (stack == null || stack.isEmpty() || parent == null || accessoryId == null) {
            return;
        }
        Set<String> ids = new HashSet<>(showing(stack));
        if (show) {
            TabulaHatDefinition accessory = parent.findAccessory(accessoryId);
            if (accessory != null && !accessory.accessoryLayers.isEmpty()) {
                for (TabulaHatDefinition other : parent.allAccessories()) {
                    if (other.id.equals(accessoryId)) {
                        continue;
                    }
                    if (sharesLayer(accessory, other) && ids.contains(other.id)) {
                        ids.remove(other.id);
                    }
                }
            }
            ids.add(accessoryId);
        } else {
            ids.remove(accessoryId);
        }
        write(stack, ids);
    }

    public static void toggle(ItemStack stack, TabulaHatDefinition parent, String accessoryId) {
        setShowing(stack, parent, accessoryId, !isShowing(stack, accessoryId));
    }

    public static boolean isBlocked(ItemStack stack, TabulaHatDefinition parent, String accessoryId) {
        if (parent == null || accessoryId == null || isShowing(stack, accessoryId)) {
            return false;
        }
        TabulaHatDefinition accessory = parent.findAccessory(accessoryId);
        if (accessory == null || accessory.accessoryLayers.isEmpty()) {
            return false;
        }
        for (TabulaHatDefinition other : parent.allAccessories()) {
            if (other.id.equals(accessoryId) || !isShowing(stack, other.id)) {
                continue;
            }
            if (sharesLayer(accessory, other)) {
                return true;
            }
        }
        return false;
    }

    public static void applyRandom(ItemStack stack, TabulaHatDefinition parent, Random random) {
        if (stack == null || stack.isEmpty() || parent == null || parent.accessories.isEmpty()) {
            return;
        }
        Set<String> ids = new HashSet<>();
        roll(parent, ids, random);
        write(stack, ids);
    }

    public static Set<String> hiddenParentParts(ItemStack stack, TabulaHatDefinition parent) {
        Set<String> hidden = new HashSet<>();
        if (parent == null) {
            return hidden;
        }
        Set<String> showing = new HashSet<>(showing(stack));
        collectHidden(parent, showing, hidden);
        return hidden;
    }

    private static void collectHidden(TabulaHatDefinition def, Set<String> showing, Set<String> hidden) {
        for (TabulaHatDefinition accessory : def.accessories) {
            if (!showing.contains(accessory.id)) {
                continue;
            }
            hidden.addAll(accessory.hideParentParts);
            collectHidden(accessory, showing, hidden);
        }
    }

    private static void roll(TabulaHatDefinition def, Set<String> ids, Random random) {
        List<TabulaHatDefinition> spawning = new ArrayList<>();
        for (TabulaHatDefinition accessory : def.accessories) {
            if (random.nextDouble() < chance(accessory.rarity)) {
                spawning.add(accessory);
            }
        }
        for (int i = spawning.size() - 1; i >= 0; i--) {
            TabulaHatDefinition accessory = spawning.get(i);
            boolean conflict = false;
            for (int j = 0; j < i; j++) {
                if (sharesLayer(accessory, spawning.get(j))) {
                    conflict = true;
                    break;
                }
            }
            if (!conflict) {
                ids.add(accessory.id);
                roll(accessory, ids, random);
            }
        }
    }

    private static double chance(HatRarity rarity) {
        return switch (rarity) {
            case COMMON -> 0.21;
            case UNCOMMON -> 0.13;
            case RARE -> 0.08;
            case EPIC -> 0.05;
            case LEGENDARY -> 0.03;
        };
    }

    private static boolean sharesLayer(TabulaHatDefinition a, TabulaHatDefinition b) {
        if (a.accessoryLayers.isEmpty() || b.accessoryLayers.isEmpty()) {
            return false;
        }
        for (String layer : a.accessoryLayers) {
            if (b.accessoryLayers.contains(layer)) {
                return true;
            }
        }
        return false;
    }

    private static void write(ItemStack stack, Set<String> ids) {
        CompoundTag tag = stack.getOrCreateTag();
        if (ids.isEmpty()) {
            tag.remove(TAG);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
            return;
        }
        ListTag list = new ListTag();
        for (String id : ids) {
            list.add(StringTag.valueOf(id));
        }
        tag.put(TAG, list);
    }
}
