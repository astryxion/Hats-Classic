package com.astryxion.astryxionshats;

import com.astryxion.astryxionshats.common.hat.HatMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Clean config for Astryxion's Hats
 */
@Mod.EventBusSubscriber(
        modid = AstryxionsHats.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /* =========================
       General Settings
       ========================= */

    public static final ForgeConfigSpec.BooleanValue ENABLE_HATS = BUILDER
            .comment("Enable or disable hats entirely")
            .define("enableHats", true);

    public static final ForgeConfigSpec.BooleanValue ENABLE_MOBS = BUILDER
            .comment("Allow mobs to wear hats")
            .define("enableMobHats", true);

    public static final ForgeConfigSpec.BooleanValue ENABLE_PLAYERS = BUILDER
            .comment("Allow players to wear hats")
            .define("enablePlayerHats", true);

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
            // 🔧 DEFAULT SET TO HUNTING
            .defineEnum("hatMode", HatMode.HUNTING);

    /* =========================
       Build Spec
       ========================= */

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    /* =========================
       Cached values
       ========================= */

    public static boolean enableHats = true;
    public static boolean enableMobHats = true;
    public static boolean enablePlayerHats = true;
    public static double mobHatSpawnChance = 0.6D;
    public static HatMode hatMode = HatMode.HUNTING;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        updateConfig(event);
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        updateConfig(event);
    }

    private static void updateConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC)
            return;

        enableHats = ENABLE_HATS.get();
        enableMobHats = ENABLE_MOBS.get();
        enablePlayerHats = ENABLE_PLAYERS.get();
        mobHatSpawnChance = MOB_HAT_SPAWN_CHANCE.get();
        hatMode = HAT_MODE.get();
    }
}