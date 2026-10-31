package com.astryxion.hats.client.network;

import com.astryxion.hats.client.gui.toast.HatToast;
import com.astryxion.hats.common.capability.HatDataAccess;
import com.astryxion.hats.common.hat.HatPartCapability;
import com.astryxion.hats.common.network.PacketHatUnlocked;
import com.astryxion.hats.common.network.PacketRehatify;
import com.astryxion.hats.common.network.PacketSyncHat;
import com.astryxion.hats.common.network.PacketSyncHatPart;
import com.astryxion.hats.common.sound.HatsSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class HatClientPacketHandlers {

    private HatClientPacketHandlers() {}

    public static void handleHatUnlocked(PacketHatUnlocked msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.getToasts().addToast(new HatToast(msg.getHatStack()));
        com.astryxion.hats.client.gui.HatsGui.refreshIfOpen();
    }

    public static void handleSyncHat(PacketSyncHat msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && msg.data() != null) {
            HatDataAccess.apply(mc.player, msg.data());
        }
    }

    public static void handleSyncHatPart(PacketSyncHatPart msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Entity entity = mc.level.getEntity(msg.playerId());
        if (entity instanceof LivingEntity living) {
            living.getData(HatPartCapability.HAT_PART).deserializeNBT(msg.tag());
        }
    }

    public static void handleRehatify(PacketRehatify msg) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(msg.entityId());
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

    public static void applyUnlockedHats(
            List<ResourceLocation> unlocked,
            @Nullable ResourceLocation equipped,
            boolean openMenu,
            List<ResourceLocation> gained,
            List<ResourceLocation> lost,
            List<Integer> counts
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        com.astryxion.hats.client.hat.ClientHatUnlocks.ensureSeeded(minecraft.player);
        if (!gained.isEmpty() || !lost.isEmpty()) {
            com.astryxion.hats.client.hat.ClientHatUnlocks.applySwap(gained, lost);
        }
        com.astryxion.hats.client.hat.ClientHatUnlocks.forceSetIfComplete(unlocked, gained, counts);
        if (minecraft.player != null) {
            HatDataAccess.replaceUnlocked(
                    minecraft.player, com.astryxion.hats.client.hat.ClientHatUnlocks.all(), equipped);
        }
        if (openMenu) {
            com.astryxion.hats.client.gui.HatsGui.open();
        } else {
            com.astryxion.hats.client.gui.HatsGui.refreshIfOpen();
        }
    }

    public static void scheduleUnlockedHats(
            List<ResourceLocation> unlocked,
            @Nullable ResourceLocation equipped,
            boolean openMenu,
            List<ResourceLocation> gained,
            List<ResourceLocation> lost,
            List<Integer> counts,
            UUID target,
            String targetName
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.player == null) {
                return;
            }
            if (!minecraft.player.getUUID().equals(target)
                    && !minecraft.player.getGameProfile().getName().equals(targetName)) {
                return;
            }
            applyUnlockedHats(unlocked, equipped, openMenu, gained, lost, counts);
        });
    }
}
