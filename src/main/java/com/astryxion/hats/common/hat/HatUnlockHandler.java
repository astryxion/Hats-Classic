package com.astryxion.hats.common.hat;

import com.astryxion.hats.Config;
import com.astryxion.hats.Hats;
import com.astryxion.hats.common.capability.HatDataCapability;
import com.astryxion.hats.common.data.ServerHatUnlocks;
import com.astryxion.hats.common.launcher.HatLaunch;
import com.astryxion.hats.common.network.HatPacketHandler;
import com.astryxion.hats.common.network.PacketHatUnlocked;
import com.astryxion.hats.common.network.PacketRehatify;
import com.astryxion.hats.common.network.PacketSetUnlockedHats;
import com.astryxion.hats.common.network.PacketSyncHat;
import com.astryxion.hats.common.trade.TradeHats;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Hunting unlocks increment stack counts. First copy also toasts and
 * advances collect milestones. Hatless living killers inherit the victim hat
 * when Mob Hat Takeover is on.
 */
@Mod.EventBusSubscriber(modid = Hats.MODID)
public class HatUnlockHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobKilled(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) {
            return;
        }

        ItemStack hatStack = HatManager.getHatStack(victim);
        ResourceLocation hatId = hatStack.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(hatStack.getItem());
        if (hatId == null || !hatId.getNamespace().equals(Hats.MODID)) {
            return;
        }

        if (event.getSource().getEntity() instanceof ServerPlayer player
                && !(player instanceof FakePlayer)
                && !player.isCreative()) {
            grantOrIncrement(player, hatId, hatStack.copy());
        } else if (Config.mobHatTakeover
                && event.getSource().getEntity() instanceof LivingEntity killer
                && killer.isAlive()
                && HatLaunch.canWear(killer)
                && HatManager.getEquippedHatId(killer) == null) {
            HatManager.setHat(killer, TradeHats.item(hatId));
            HatPacketHandler.syncHatPartToTracking(killer);
            if (killer.level() instanceof ServerLevel server) {
                for (ServerPlayer observer : server.getServer().getPlayerList().getPlayers()) {
                    if (observer.level() == server && observer.distanceToSqr(killer) < 64 * 64) {
                        HatPacketHandler.sendToPlayer(observer, new PacketRehatify(killer.getId()));
                    }
                }
            }
        }

        HatManager.clearHat(victim);
    }

    private static void grantOrIncrement(ServerPlayer player, ResourceLocation hatId, ItemStack toastStack) {
        ServerHatUnlocks store = ServerHatUnlocks.get(player);
        boolean firstCopy = store.add(player, hatId);
        PacketSetUnlockedHats.send(player);
        HatAdvancements.onUnlockCounts(player, hatId);

        if (!firstCopy) {
            return;
        }

        player.getCapability(HatDataCapability.HAT_DATA).ifPresent(data -> {
            int totalUnlocked = store.copy(player).size();
            CompoundTag unlocked = data.serializeNBT();
            com.astryxion.hats.common.capability.HatDataAccess.apply(player, unlocked);
            HatPacketHandler.sendToPlayer(player, new PacketHatUnlocked(toastStack));
            HatPacketHandler.sendToPlayer(player, new PacketSyncHat(unlocked));
            player.getInventory().setChanged();
            if (Config.hatUnlockAnnouncement) {
                broadcastHatUnlock(player, toastStack, hatId);
            }
            Hats.LOGGER.info("Unlocked hat: {} | Total: {}", hatId, totalUnlocked);
        });
    }

    private static void broadcastHatUnlock(ServerPlayer player, ItemStack hat, ResourceLocation hatId) {
        Item item = hat.getItem();
        HatRarity rarity = item instanceof HatItem hatItem ? hatItem.getHatRarity() : HatRarity.COMMON;
        TextColor color = TextColor.fromRgb(rarity.getColor());
        String displayName = hat.getHoverName().getString();
        String rarityLabel = rarity.name().charAt(0) + rarity.name().substring(1).toLowerCase();

        MutableComponent tooltip = Component.literal(displayName).withStyle(style -> style.withColor(color));
        tooltip.append(Component.literal("\n"));
        tooltip.append(Component.literal(rarityLabel)
                .withStyle(style -> style.withColor(color).withItalic(true)));
        if (HatContributor.isContributor(item, hatId)) {
            tooltip.append(Component.literal("\n\nContributor hat")
                    .withStyle(style -> style.withColor(color).withItalic(true)));
        }

        StringBuilder insertion = new StringBuilder("hats:model:").append(hatId);
        boolean firstAccessory = true;
        for (String accessory : HatAccessories.showing(hat)) {
            insertion.append(firstAccessory ? "|" : ",").append(accessory);
            firstAccessory = false;
        }

        MutableComponent hatName = Component.literal(displayName).withStyle(style -> style
                .withColor(color)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))
                .withInsertion(insertion.toString()));

        player.server.getPlayerList().broadcastSystemMessage(
                Component.literal("")
                        .append(player.getDisplayName())
                        .append(Component.literal(" unlocked "))
                        .append(hatName)
                        .append(Component.literal("!")),
                false);
    }
}
