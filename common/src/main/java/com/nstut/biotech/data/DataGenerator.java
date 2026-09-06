package com.nstut.biotech.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public abstract class DataGenerator {
    private static final String RECIPE_DIRECTORY = System.getProperty("biotech.recipeDir", "recipe");
    private static final String LOOT_TABLE_DIRECTORY = System.getProperty("biotech.lootTableDir", "loot_table");

    protected static final String GEN_DATA_PATH = "src/generated/resources/data/biotech";
    private static final String GEN_LOOT_TABLES_PATH = GEN_DATA_PATH + "/" + LOOT_TABLE_DIRECTORY;
    protected static final String GEN_BLOCK_LOOT_TABLES_PATH = GEN_LOOT_TABLES_PATH + "/blocks";
    protected static final String GEN_RECIPES_PATH = GEN_DATA_PATH + "/" + RECIPE_DIRECTORY;

    protected static final String GEN_ASSETS_PATH = "src/generated/resources/assets/biotech";
    protected static final String GEN_BLOCKSTATES_PATH = GEN_ASSETS_PATH + "/blockstates";
    protected static final String GEN_BLOCK_MODELS_PATH = GEN_ASSETS_PATH + "/models/block";
    protected static final String GEN_ITEM_MODELS_PATH = GEN_ASSETS_PATH + "/models/item";
    protected static final String GEN_BLOCK_TEXTURE_PATH = GEN_ASSETS_PATH + "/textures/block";

    private static final String SOURCE_RESOURCES = System.getProperty("biotech.sourceResources", "src/main/resources");
    protected static final String ASSETS_PATH = SOURCE_RESOURCES + "/assets/biotech";
    protected static final String BLOCK_TEXTURE_PATH = ASSETS_PATH + "/textures/block";

    private static void clearDirectory(String path) {
        Path root = Paths.get(path);
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(root))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new IllegalStateException("Failed to delete generated resource " + p, e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to clear generated resource directory " + path, e);
        }
    }

    private static void createAndClearDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
            clearDirectory(path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create generated resource directory " + path, e);
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

        new RecipeGenerator().generate();
        new MachineGenerator().generate();
    }
}
