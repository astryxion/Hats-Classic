package com.astryxion.hats;

import com.astryxion.hats.Config;
import com.astryxion.hats.common.hat.GuiStyle;
import com.astryxion.hats.common.hat.HatMode;
import com.astryxion.hats.common.hatstand.HatStandGuiStyle;
import com.astryxion.hats.common.spawn.MobHatPolicy;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Common configuration for Hats.
 */
@Mod.EventBusSubscriber(
        modid = Hats.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class Config {

    /**
     * Vanilla mobs that can spawn with and wear hats. Matches Hats Renewed's
     * registered set, plus Classic extras (giant, squid, undead horses).
     */
    public static final List<EntityType<?>> HAT_MOB_TYPES = List.of(
            // Passive / neutral
            EntityType.ALLAY,
            EntityType.AXOLOTL,
            EntityType.BAT,
            EntityType.BEE,
            EntityType.CAMEL,
            EntityType.CAT,
            EntityType.CHICKEN,
            EntityType.COD,
            EntityType.COW,
            EntityType.DOLPHIN,
            EntityType.DONKEY,
            EntityType.FOX,
            EntityType.FROG,
            EntityType.GLOW_SQUID,
            EntityType.GOAT,
            EntityType.HORSE,
            EntityType.IRON_GOLEM,
            EntityType.LLAMA,
            EntityType.MOOSHROOM,
            EntityType.MULE,
            EntityType.OCELOT,
            EntityType.PANDA,
            EntityType.PARROT,
            EntityType.PIG,
            EntityType.POLAR_BEAR,
            EntityType.RABBIT,
            EntityType.SALMON,
            EntityType.SHEEP,
            EntityType.SKELETON_HORSE,
            EntityType.SNIFFER,
            EntityType.SNOW_GOLEM,
            EntityType.SQUID,
            EntityType.STRIDER,
            EntityType.TADPOLE,
            EntityType.TRADER_LLAMA,
            EntityType.TROPICAL_FISH,
            EntityType.TURTLE,
            EntityType.VEX,
            EntityType.VILLAGER,
            EntityType.WANDERING_TRADER,
            EntityType.WARDEN,
            EntityType.WOLF,
            EntityType.ZOMBIE_HORSE,
            // Hostile
            EntityType.BLAZE,
            EntityType.CAVE_SPIDER,
            EntityType.CREEPER,
            EntityType.DROWNED,
            EntityType.ELDER_GUARDIAN,
            EntityType.ENDER_DRAGON,
            EntityType.ENDERMAN,
            EntityType.ENDERMITE,
            EntityType.EVOKER,
            EntityType.GHAST,
            EntityType.GIANT,
            EntityType.GUARDIAN,
            EntityType.HOGLIN,
            EntityType.HUSK,
            EntityType.ILLUSIONER,
            EntityType.MAGMA_CUBE,
            EntityType.PHANTOM,
            EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.PILLAGER,
            EntityType.PUFFERFISH,
            EntityType.RAVAGER,
            EntityType.SHULKER,
            EntityType.SILVERFISH,
            EntityType.SKELETON,
            EntityType.SLIME,
            EntityType.SPIDER,
            EntityType.STRAY,
            EntityType.VINDICATOR,
            EntityType.WITCH,
            EntityType.WITHER,
            EntityType.WITHER_SKELETON,
            EntityType.ZOGLIN,
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN
    );

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue SHOW_WELCOME_MESSAGE = BUILDER
            .comment("Show the Hats welcome chat message when joining a world")
            .define("showWelcomeMessage", true);

    public static final ForgeConfigSpec.DoubleValue MOB_HAT_SPAWN_CHANCE = BUILDER
            .comment(
                    "Chance for mobs to spawn wearing hats",
                    "0.0 = never",
                    "1.0 = always"
            )
            .defineInRange("mobHatSpawnChance", 0.6D, 0.0D, 1.0D);

    public static final ForgeConfigSpec.EnumValue<HatMode> HAT_MODE = BUILDER
            .comment(
                    "Hat gameplay mode",
                    "COSMETIC = all hats unlocked",
                    "HUNTING = hats must be unlocked by killing mobs"
            )
            .defineEnum("hatMode", HatMode.HUNTING);

    public static final ForgeConfigSpec.EnumValue<GuiStyle> GUI_STYLE = BUILDER
            .comment(
                    "Hat menu style",
                    "LEGACY = Hats 1.7.10 window",
                    "MODERN = Hats 1.16.5 / Renewed grid"
            )
            .defineEnum("guiStyle", GuiStyle.LEGACY);

    public static final ForgeConfigSpec.BooleanValue SHOW_RARITY_COLORS_EVERYWHERE = BUILDER
            .comment("Color Legacy GUI hat names by rarity outside the rarity categories")
            .define("showRarityColorsEverywhere", false);

    public static final ForgeConfigSpec.EnumValue<HatStandGuiStyle> HAT_STAND_GUI_STYLE = BUILDER
            .comment("Hat stand menu style")
            .defineEnum("hatStandGuiStyle", HatStandGuiStyle.LEGACY);

    public static final ForgeConfigSpec.BooleanValue MOB_HAT_TAKEOVER = BUILDER
            .comment("When a hatless mob kills a hatted mob, the killer inherits the hat")
            .define("mobHatTakeover", true);

    public static final ForgeConfigSpec.BooleanValue BOSS_MOB_HATS = BUILDER
            .comment(
                    "When true, boss mobs (ender dragon, wither, elder guardian, warden)",
                    "can spawn with hats. When false they never spawn hatted;",
                    "per-mob toggles still apply when this is on."
            )
            .define("bossMobHats", true);

    public static final ForgeConfigSpec.BooleanValue PREVENT_UNDEAD_FIRE = BUILDER
            .comment(
                    "When true, undead mobs wearing a hat do not burn in sunlight.",
                    "Default: false."
            )
            .define("preventUndeadFire", false);

    public static final ForgeConfigSpec.BooleanValue DISABLE_CONTRIBUTOR_HATS = BUILDER
            .comment(
                    "When true, (C) contributor hats are excluded from random",
                    "mob hat spawn rolls. Already worn hats are left alone. Default: false."
            )
            .define("disableContributorHats", false);

    public static final ForgeConfigSpec.IntValue HAT_ENTITY_LIFESPAN = BUILDER
            .comment("Ticks a launched hat entity lives before disappearing")
            .defineInRange("hatEntityLifespan", 6000, 20, 72000);

    public static final ForgeConfigSpec.BooleanValue HAT_LAUNCHER_REPLACES_PLAYER_HAT = BUILDER
            .comment("Launched hats can knock a hat off another player")
            .define("hatLauncherReplacesPlayerHat", true);

    public static final ForgeConfigSpec.BooleanValue HAT_LAUNCHER_KEEPS_INVENTORY_HAT = BUILDER
            .comment("When a launched hat hits a player, keep their previous hat in inventory")
            .define("hatLauncherDoesNotRemoveHatFromInventory", true);

    public static final ForgeConfigSpec.BooleanValue HAT_UNLOCK_ANNOUNCEMENT = BUILDER
            .comment(
                    "If true, a chat message is broadcast to all players whenever one",
                    "of them unlocks a new hat for the first time. The hat name is",
                    "colored by rarity and hovering shows its info. Default: false."
            )
            .define("hatUnlockChatAnnouncement", false);

    public static final Map<String, ForgeConfigSpec.BooleanValue> MOB_HAT_TOGGLES = new LinkedHashMap<>();

    static {
        BUILDER.push("mobHats");
        BUILDER.comment("Toggle hats per mob.");
        for (EntityType<?> type : HAT_MOB_TYPES) {
            String key = mobKey(type);
            MOB_HAT_TOGGLES.put(key, BUILDER
                    .comment("Allow " + key.replace('_', ' ') + " to spawn with hats")
                    .define(key, true));
        }
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean showWelcomeMessage = true;
    public static double mobHatSpawnChance = 0.6D;
    public static HatMode hatMode = HatMode.HUNTING;
    public static GuiStyle guiStyle = GuiStyle.LEGACY;
    public static boolean showRarityColorsEverywhere;
    public static HatStandGuiStyle hatStandGuiStyle = HatStandGuiStyle.LEGACY;
    public static boolean mobHatTakeover = true;
    public static boolean bossMobHats = true;
    public static boolean preventUndeadFire = false;
    public static boolean disableContributorHats = false;
    public static int hatEntityLifespan = 6000;
    public static boolean hatLauncherReplacesPlayerHat = true;
    public static boolean hatLauncherDoesNotRemoveHatFromInventory = true;
    public static boolean hatUnlockAnnouncement = false;
    public static final Map<String, Boolean> mobHatsEnabled = new LinkedHashMap<>();

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        updateConfig(event);
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        updateConfig(event);
    }

    private static void updateConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        showWelcomeMessage = SHOW_WELCOME_MESSAGE.get();
        mobHatSpawnChance = MOB_HAT_SPAWN_CHANCE.get();
        hatMode = HAT_MODE.get();
        guiStyle = GUI_STYLE.get();
        showRarityColorsEverywhere = SHOW_RARITY_COLORS_EVERYWHERE.get();
        hatStandGuiStyle = HAT_STAND_GUI_STYLE.get();
        mobHatTakeover = MOB_HAT_TAKEOVER.get();
        bossMobHats = BOSS_MOB_HATS.get();
        preventUndeadFire = PREVENT_UNDEAD_FIRE.get();
        disableContributorHats = DISABLE_CONTRIBUTOR_HATS.get();
        hatEntityLifespan = HAT_ENTITY_LIFESPAN.get();
        hatLauncherReplacesPlayerHat = HAT_LAUNCHER_REPLACES_PLAYER_HAT.get();
        hatLauncherDoesNotRemoveHatFromInventory = HAT_LAUNCHER_KEEPS_INVENTORY_HAT.get();
        hatUnlockAnnouncement = HAT_UNLOCK_ANNOUNCEMENT.get();
        mobHatsEnabled.clear();
        for (Map.Entry<String, ForgeConfigSpec.BooleanValue> entry : MOB_HAT_TOGGLES.entrySet()) {
            mobHatsEnabled.put(entry.getKey(), entry.getValue().get());
        }
        MobHatPolicy.applyToLoadedWorlds();
    }

    public static String mobKey(EntityType<?> type) {
        var id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return id != null ? id.getPath() : "unknown";
    }

    public static boolean canMobWearHats(EntityType<?> type) {
        String key = mobKey(type);
        if (!MOB_HAT_TOGGLES.containsKey(key)) {
            return false;
        }
        return mobHatsEnabled.getOrDefault(key, true);
    }

    public static void setShowWelcomeMessage(boolean value) {
        SHOW_WELCOME_MESSAGE.set(value);
        showWelcomeMessage = value;
    }

    public static void setMobHatSpawnChance(double value) {
        double clamped = Math.max(0.0D, Math.min(1.0D, value));
        MOB_HAT_SPAWN_CHANCE.set(clamped);
        mobHatSpawnChance = clamped;
    }

    public static void setHatMode(HatMode mode) {
        HAT_MODE.set(mode);
        hatMode = mode;
    }

    public static void setGuiStyle(GuiStyle style) {
        GUI_STYLE.set(style);
        guiStyle = style;
    }

    public static void setShowRarityColorsEverywhere(boolean value) {
        SHOW_RARITY_COLORS_EVERYWHERE.set(value);
        showRarityColorsEverywhere = value;
    }

    public static void setHatStandGuiStyle(HatStandGuiStyle value) {
        HAT_STAND_GUI_STYLE.set(value);
        hatStandGuiStyle = value;
    }

    public static void setMobHatTakeover(boolean value) {
        MOB_HAT_TAKEOVER.set(value);
        mobHatTakeover = value;
    }

    public static void setBossMobHats(boolean value) {
        boolean previous = bossMobHats;
        BOSS_MOB_HATS.set(value);
        bossMobHats = value;
        if (previous != value) {
            MobHatPolicy.applyToLoadedWorlds();
        }
    }

    public static void setPreventUndeadFire(boolean value) {
        PREVENT_UNDEAD_FIRE.set(value);
        preventUndeadFire = value;
    }

    public static void setDisableContributorHats(boolean value) {
        DISABLE_CONTRIBUTOR_HATS.set(value);
        disableContributorHats = value;
    }

    public static boolean isBossHatMob(EntityType<?> type) {
        return type == EntityType.ENDER_DRAGON
                || type == EntityType.WITHER
                || type == EntityType.ELDER_GUARDIAN
                || type == EntityType.WARDEN;
    }

    /** Spawn / random hats: per-mob toggle plus the boss master switch. */
    public static boolean canMobSpawnWithHats(EntityType<?> type) {
        if (!canMobWearHats(type)) {
            return false;
        }
        return bossMobHats || !isBossHatMob(type);
    }

    public static void setHatLauncherReplacesPlayerHat(boolean value) {
        HAT_LAUNCHER_REPLACES_PLAYER_HAT.set(value);
        hatLauncherReplacesPlayerHat = value;
    }

    public static void setHatLauncherKeepsInventoryHat(boolean value) {
        HAT_LAUNCHER_KEEPS_INVENTORY_HAT.set(value);
        hatLauncherDoesNotRemoveHatFromInventory = value;
    }

    public static void setHatUnlockAnnouncement(boolean value) {
        HAT_UNLOCK_ANNOUNCEMENT.set(value);
        hatUnlockAnnouncement = value;
    }

    public static void setMobHatEnabled(EntityType<?> type, boolean value) {
        String key = mobKey(type);
        ForgeConfigSpec.BooleanValue spec = MOB_HAT_TOGGLES.get(key);
        if (spec == null) {
            return;
        }
        boolean previous = mobHatsEnabled.getOrDefault(key, true);
        spec.set(value);
        mobHatsEnabled.put(key, value);
        if (previous != value) {
            MobHatPolicy.applyToLoadedWorlds();
        }
    }

    public static void setAllMobHats(boolean value) {
        boolean changed = false;
        for (EntityType<?> type : HAT_MOB_TYPES) {
            String key = mobKey(type);
            ForgeConfigSpec.BooleanValue spec = MOB_HAT_TOGGLES.get(key);
            if (spec == null) {
                continue;
            }
            boolean previous = mobHatsEnabled.getOrDefault(key, true);
            spec.set(value);
            mobHatsEnabled.put(key, value);
            changed |= previous != value;
        }
        if (changed) {
            MobHatPolicy.applyToLoadedWorlds();
        }
    }

    public static void save() {
        SPEC.save();
    }
}
