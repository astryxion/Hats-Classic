package com.astryxion.hats.client.render;

import com.astryxion.hats.common.hat.HatManager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hats Renewed {@code ClientHatCache}: the hat layer reads this first so GUI
 * cells can show a hat that is not the one the live player is wearing.
 */
public final class ClientHatPreview {

    private static final Map<UUID, ItemStack> OVERRIDES = new HashMap<>();
    private static final ThreadLocal<ItemStack> ACTIVE = new ThreadLocal<>();

    private ClientHatPreview() {}

    public static void begin(ItemStack stack) {
        ACTIVE.set(stack == null ? ItemStack.EMPTY : stack);
    }

    public static void end() {
        ACTIVE.remove();
    }

    public static boolean isRendering() {
        return ACTIVE.get() != null;
    }

    public static void set(UUID id, ItemStack stack) {
        OVERRIDES.put(id, stack == null ? ItemStack.EMPTY : stack);
    }

    public static void clear(UUID id) {
        OVERRIDES.remove(id);
    }

    @Nullable
    public static ItemStack get(UUID id) {
        return OVERRIDES.get(id);
    }

    public static ItemStack orWorn(LivingEntity entity) {
        ItemStack active = ACTIVE.get();
        if (active != null) {
            return active;
        }
        if (entity == null) {
            return ItemStack.EMPTY;
        }
        ItemStack override = OVERRIDES.get(entity.getUUID());
        if (override != null) {
            return override;
        }
        return HatManager.getHat(entity);
    }
}
