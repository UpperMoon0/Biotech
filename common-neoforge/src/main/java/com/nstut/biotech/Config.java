package com.nstut.biotech;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue IS_DEV_ENV = BUILDER
            .comment("Toggle development environment")
            .define("isDevEnv", false);

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SLAUGHTERHOUSE_YIELD_MULTIPLIER = SERVER_BUILDER
            .comment("Post-roll item-count multiplier for dynamic Slaughterhouse entity loot. New transactions only.",
                    "Explicit itemOutputs in datapack recipes are static and are not multiplied.")
            .defineInRange("slaughterhouseYieldMultiplier", 2, 1, 64);

    /** Cached config value; initialized for tests and callers before the config load event. */
    public static int slaughterhouseYieldMultiplier = SlaughterhouseYieldConfig.DEFAULT;

    static final ModConfigSpec SPEC = BUILDER.build();
    static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    public static boolean isDevEnv;

    private Config() {
    }

    static void onLoad(final ModConfigEvent event) {
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
