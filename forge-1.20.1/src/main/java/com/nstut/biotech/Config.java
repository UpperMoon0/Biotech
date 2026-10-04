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
            return;
        }
        if (event.getConfig().getSpec() != SPEC) return;
        isDevEnv = !(event instanceof ModConfigEvent.Unloading) && IS_DEV_ENV.get();
        Biotech.IS_DEV_ENV = isDevEnv;
    }
}