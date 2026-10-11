package com.nstut.biotech;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Biotech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue IS_DEV_ENV = BUILDER
            .comment("Toggle development environment")
            .define("isDevEnv", false);

    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue SLAUGHTERHOUSE_YIELD_MULTIPLIER = SERVER_BUILDER
            .comment("Post-roll item-count multiplier for dynamic Slaughterhouse entity loot. New transactions only.",
                    "Explicit itemOutputs in datapack recipes are static and are not multiplied.")
            .defineInRange("slaughterhouseYieldMultiplier", 2, 1, 64);

    public static final ForgeConfigSpec.DoubleValue MACHINE_ENERGY_MULTIPLIER = SERVER_BUILDER
            .comment("Scales datapack totalEnergy when a NEW cycle starts. Active saved cycles keep their cost.")
            .defineInRange("machineEnergyMultiplier", 1.0, 0.01, 100.0);
    public static final ForgeConfigSpec.IntValue MACHINE_ENERGY_PER_TICK = SERVER_BUILDER
            .comment("Maximum energy consumed per controller tick. Does not change hatch capacity or energy acceptance.")
            .defineInRange("machineEnergyPerTick", 512, 1, 65536);
    public static double machineEnergyMultiplier = 1.0;
    public static int machineEnergyPerTick = 512;

    /** Cached config value; initialized for tests and callers before the config load event. */
    public static int slaughterhouseYieldMultiplier = SlaughterhouseYieldConfig.DEFAULT;

    static final ForgeConfigSpec SPEC = BUILDER.build();
    static final ForgeConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    public static boolean isDevEnv;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            slaughterhouseYieldMultiplier = SlaughterhouseYieldConfig.forEvent(
                    event instanceof ModConfigEvent.Unloading, SLAUGHTERHOUSE_YIELD_MULTIPLIER::get);
            boolean unloading = event instanceof ModConfigEvent.Unloading;
            machineEnergyMultiplier = unloading ? 1.0 : MACHINE_ENERGY_MULTIPLIER.get();
            machineEnergyPerTick = unloading ? 512 : MACHINE_ENERGY_PER_TICK.get();
            return;
        }
        if (event.getConfig().getSpec() != SPEC) return;
        isDevEnv = !(event instanceof ModConfigEvent.Unloading) && IS_DEV_ENV.get();
        Biotech.IS_DEV_ENV = isDevEnv;
    }
}