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

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean isDevEnv;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        isDevEnv = IS_DEV_ENV.get();
        Biotech.IS_DEV_ENV = isDevEnv;
    }
}