package com.nstut.biotech;

import com.mojang.logging.LogUtils;
import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.BlockEntityRegistries;
import com.nstut.biotech.items.ItemRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
// @Mod(Biotech.MOD_ID) // Removed Forge-specific annotation
public class Biotech
{
    public static boolean IS_DEV_ENV;

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "biotech";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        // This is where common initialization logic will go.
        LOGGER.info("Biotech common initializing...");

        // Register blocks, items, and block entities
        BlockRegistries.register();
        ItemRegistries.register();
        BlockEntityRegistries.register();

        // We will add other registries here as we migrate them (e.g., Menus, etc.)

        LOGGER.info("Biotech common initialized.");
    }
}
