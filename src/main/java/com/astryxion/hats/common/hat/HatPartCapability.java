package com.astryxion.hats.common.hat;

import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HatPart storage for LivingEntity (NeoForge – in-memory map)
 */
public class HatPartCapability {

    private static final Map<String, HatPart> STORAGE = new ConcurrentHashMap<>();

    private static String key(LivingEntity entity) {
        if (entity instanceof net.minecraft.world.entity.player.Player) {
            return "player:" + entity.getUUID().toString();
        }
        return "entity:" + entity.level().dimension().location() + ":" + entity.getId();
    }

    public static HatPart get(LivingEntity entity) {
        if (entity == null) return null;
        return STORAGE.get(key(entity));
    }

    public static HatPart getOrCreate(LivingEntity entity) {
        if (entity == null) return null;
        String k = key(entity);
        return STORAGE.computeIfAbsent(k, x -> new HatPart());
    }

    public static void remove(LivingEntity entity) {
        if (entity != null) STORAGE.remove(key(entity));
    }
}
