package com.astryxion.hats.client.network;

import com.astryxion.hats.client.gui.HatsGui;
import com.astryxion.hats.client.gui.toast.HatToast;
import com.astryxion.hats.client.hat.ClientHatUnlocks;
import com.astryxion.hats.common.capability.HatDataAccess;
import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.network.PacketHatUnlocked;
import com.astryxion.hats.common.sound.HatsSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class HatClientPacketHandlers {

    private static final Map<Integer, PendingHatPart> PENDING_HAT_PARTS = new HashMap<>();

    private HatClientPacketHandlers() {}

    public static void handleHatUnlocked(PacketHatUnlocked msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.getToastManager().addToast(new HatToast(msg.getHatStack()));
        HatsGui.refreshIfOpen();
    }

    public static void handleSyncHat(@Nullable CompoundTag data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && data != null) {
            HatDataAccess.apply(mc.player, data);
        }
    }

    public static void handleSyncHatPart(int playerId, @Nullable CompoundTag tag) {
        if (!applyHatPart(playerId, tag) && tag != null) {
            PENDING_HAT_PARTS.put(playerId, new PendingHatPart(tag.copy()));
        }
    }

    /** Spawn packets can arrive before the client has the entity. */
    public static void flushPendingHatParts() {
        if (PENDING_HAT_PARTS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            PENDING_HAT_PARTS.clear();
            return;
        }
        Iterator<Map.Entry<Integer, PendingHatPart>> it = PENDING_HAT_PARTS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, PendingHatPart> entry = it.next();
            if (applyHatPart(entry.getKey(), entry.getValue().tag) || ++entry.getValue().ticks > 40) {
                it.remove();
            }
        }
    }

    public static void handleRehatify(int entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(entityId);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        living.playSound(HatsSounds.POOF.get(), 1.0F,
                1.0F + (living.getRandom().nextFloat() * 2F - 1F) * 0.15F);
        for (int i = 0; i < 20; i++) {
            minecraft.level.addParticle(ParticleTypes.POOF,
                    living.getRandomX(1.0D), living.getRandomY(), living.getRandomZ(1.0D),
                    living.getRandom().nextGaussian() * 0.02D,
                    living.getRandom().nextGaussian() * 0.02D,
                    living.getRandom().nextGaussian() * 0.02D);
        }
        for (int i = 0; i < 10; i++) {
            minecraft.level.addParticle(ParticleTypes.FIREWORK,
                    living.getRandomX(1.0D), living.getRandomY(), living.getRandomZ(1.0D),
                    living.getRandom().nextGaussian() * 0.03D,
                    living.getRandom().nextGaussian() * 0.03D,
                    living.getRandom().nextGaussian() * 0.03D);
        }
    }

    public static void applyUnlocked(
            List<Identifier> unlocked,
            @Nullable Identifier equipped,
            boolean openMenu,
            List<Identifier> gained,
            List<Identifier> lost,
            List<Integer> counts
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientHatUnlocks.ensureSeeded(minecraft.player);
        if (!gained.isEmpty() || !lost.isEmpty()) {
            ClientHatUnlocks.applySwap(gained, lost);
        }
        ClientHatUnlocks.forceSetIfComplete(unlocked, gained, counts);
        if (minecraft.player != null) {
            HatDataAccess.replaceUnlocked(minecraft.player, ClientHatUnlocks.all(), equipped);
        }
        if (openMenu) {
            HatsGui.open();
        } else {
            HatsGui.refreshIfOpen();
        }
    }

    /**
     * Integrated-server host often never receives its own S2C packets. Apply them
     * on the client thread when the local player is the target.
     */
    public static void applyUnlockedLocal(
            UUID playerId,
            String playerName,
            List<Identifier> unlocked,
            @Nullable Identifier equipped,
            boolean openMenu,
            List<Identifier> gained,
            List<Identifier> lost,
            List<Integer> counts
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.player == null) {
                return;
            }
            if (!minecraft.player.getUUID().equals(playerId)
                    && !minecraft.player.getGameProfile().name().equals(playerName)) {
                return;
            }
            applyUnlocked(unlocked, equipped, openMenu, gained, lost, counts);
        });
    }

    private static boolean applyHatPart(int entityId, @Nullable CompoundTag tag) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || tag == null) {
            return false;
        }
        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        living.getData(HatPartCapability.HAT_PART).deserializeNBT(tag);
        return true;
    }

    private static final class PendingHatPart {
        private final CompoundTag tag;
        private int ticks;

        private PendingHatPart(CompoundTag tag) {
            this.tag = tag;
        }
    }
}
