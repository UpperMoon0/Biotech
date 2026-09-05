package com.nstut.biotech.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.logging.Logger;

public abstract class DataGenerator {

    private static final Logger LOGGER = Logger.getLogger(DataGenerator.class.getName());

    protected static final String GEN_DATA_PATH = "src/generated/resources/data/biotech";
    private static final String GEN_LOOT_TABLES_PATH = GEN_DATA_PATH + "/loot_tables";
    protected static final String GEN_BLOCK_LOOT_TABLES_PATH = GEN_LOOT_TABLES_PATH + "/blocks";
    protected static final String GEN_RECIPES_PATH = GEN_DATA_PATH + "/recipes";

    protected static final String GEN_ASSETS_PATH = "src/generated/resources/assets/biotech";
    protected static final String GEN_BLOCKSTATES_PATH = GEN_ASSETS_PATH + "/blockstates";
    protected static final String GEN_BLOCK_MODELS_PATH = GEN_ASSETS_PATH + "/models/block";
    protected static final String GEN_ITEM_MODELS_PATH = GEN_ASSETS_PATH + "/models/item";
    protected static final String GEN_BLOCK_TEXTURE_PATH = GEN_ASSETS_PATH + "/textures/block";

    protected static final String ASSETS_PATH = "src/main/resources/assets/biotech";
    protected static final String BLOCK_TEXTURE_PATH = ASSETS_PATH + "/textures/block";

    private static void clearDirectory(String path) {
        try (var paths = Files.walk(Paths.get(path))) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(Paths.get(path))) // Skip the root directory
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            LOGGER.warning("Failed to delete " + p);
                        }
                    });
        } catch (IOException e) {
            LOGGER.warning("Failed to clear directory " + path);
        }
    }

    private static void createAndClearDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
            clearDirectory(path);
        } catch (IOException e) {
            LOGGER.warning("Failed to create or clear directory " + path);
        }
    }

    protected abstract void generate();

    public static void main(String[] args) {
        createAndClearDirectory(GEN_BLOCKSTATES_PATH);
        createAndClearDirectory(GEN_BLOCK_MODELS_PATH);
        createAndClearDirectory(GEN_ITEM_MODELS_PATH);
        createAndClearDirectory(GEN_BLOCK_TEXTURE_PATH);
        createAndClearDirectory(GEN_BLOCK_LOOT_TABLES_PATH);
        createAndClearDirectory(GEN_RECIPES_PATH);

        RecipeGenerator recipeGenerator = new RecipeGenerator();
        recipeGenerator.generate();

        MachineGenerator machineGenerator = new MachineGenerator();
        machineGenerator.generate();
    }
}