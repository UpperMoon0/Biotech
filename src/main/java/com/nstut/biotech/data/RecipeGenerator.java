package com.nstut.biotech.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nstut.biotech.Biotech;
import com.nstut.biotech.models.Creature;
import com.nstut.biotech.models.Crop;
import com.nstut.biotech.models.Food;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class RecipeGenerator extends DataGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = Logger.getLogger(RecipeGenerator.class.getName());

    @Override
    public void generate() {
        generateBreedingChamberRecipes();
        generateTerrestrialHabitatRecipes();
        generateSlaughterhouseRecipes();
        generateGreenhouseRecipes();
    }

    public void generateBreedingChamberRecipes() {
        String machineId = "breeding_chamber";
        String type = Biotech.MOD_ID + ":" + machineId;

        List<Creature> creatures = CreatureData.CREATURES;

        for (Creature creature : creatures) {
            List<Food> foods = CreatureData.FOODS.get(creature);

            for (Food food : foods) {
                IngredientItemJsonObj[] ingredientItems = new IngredientItemJsonObj[]{
                        new IngredientItemJsonObj(new ItemStackJsonObj(creature.id(), 2), false),
                        new IngredientItemJsonObj(new ItemStackJsonObj(food.id(), 2), true)
                };
                FluidJsonObj[] fluidInputs = new FluidJsonObj[]{
                        new FluidJsonObj(CreatureData.FLUID_WATER, 200)
                };
                OutputItemJsonObj[] outputItems = new OutputItemJsonObj[]{
                        new OutputItemJsonObj(new ItemStackJsonObj(creature.babyId(), 1))
                };
                FluidJsonObj[] fluidOutputs = new FluidJsonObj[]{};
                int energy = 20000;

                String creatureName = creature.id().substring(creature.id().indexOf(":") + 1);
                String foodName = food.id().substring(food.id().indexOf(":") + 1);
                String recipeName = machineId + "_" + creatureName + "_t" + food.tier() + "_" + foodName;

                RecipeJson recipeJson = new RecipeJson(type, ingredientItems, outputItems, fluidInputs, fluidOutputs, energy);

                generateRecipe(recipeName, recipeJson);
            }
        }
    }

    public void generateTerrestrialHabitatRecipes() {
        String machineId = "terrestrial_habitat";
        String type = Biotech.MOD_ID + ":" + machineId;

        List<Creature> creatures = CreatureData.CREATURES;

        for (Creature creature : creatures) {
            List<Food> foods = CreatureData.FOODS.get(creature);

            for (Food food : foods) {
                IngredientItemJsonObj[] itemInputs = new IngredientItemJsonObj[]{
                        new IngredientItemJsonObj(new ItemStackJsonObj(creature.babyId(), 2), true),
                        new IngredientItemJsonObj(new ItemStackJsonObj(food.id(), 4), true)
                };
                FluidJsonObj[] fluidInputs = new FluidJsonObj[]{
                        new FluidJsonObj(CreatureData.FLUID_WATER, 500)
                };
                OutputItemJsonObj[] itemOutputs = new OutputItemJsonObj[]{
                        new OutputItemJsonObj(new ItemStackJsonObj(creature.id(), 1))
                };
                FluidJsonObj[] fluidOutputs = new FluidJsonObj[]{};
                int energy = 48000;

                String creatureName = creature.id().substring(creature.id().indexOf(":") + 1);
                String foodName = food.id().substring(food.id().indexOf(":") + 1);
                String recipeName = machineId + "_" + creatureName + "_t" + food.tier() + "_" + foodName;

                RecipeJson recipeJson = new RecipeJson(type, itemInputs, itemOutputs, fluidInputs, fluidOutputs, energy);

                generateRecipe(recipeName, recipeJson);
            }
        }
    }

    public void generateSlaughterhouseRecipes() {
        String machineId = "slaughterhouse";
        String type = Biotech.MOD_ID + ":" + machineId;

        List<Creature> creatures = CreatureData.CREATURES;

        for (Creature creature : creatures) {
            IngredientItemJsonObj[] ingredientItems = new IngredientItemJsonObj[]{
                    new IngredientItemJsonObj(new ItemStackJsonObj(creature.id(), 1), true)
            };
            FluidJsonObj[] fluidInputs = new FluidJsonObj[]{
                    new FluidJsonObj(CreatureData.FLUID_WATER, 200)
            };
            OutputItemJsonObj[] outputItems = CreatureData.DROPS.get(creature).stream()
                    .map(d -> new OutputItemJsonObj(new ItemStackJsonObj(d.id(), d.count()), d.chance())).toArray(OutputItemJsonObj[]::new);
            FluidJsonObj[] fluidOutputs = new FluidJsonObj[]{};
            int energy = 16000;

            String creatureName = creature.id().substring(creature.id().indexOf(":") + 1);
            String recipeName = machineId + "_" + creatureName;

            RecipeJson recipeJson = new RecipeJson(type, ingredientItems, outputItems, fluidInputs, fluidOutputs, energy);

            generateRecipe(recipeName, recipeJson);
        }
    }

    public void generateGreenhouseRecipes() {
        String machineId = "greenhouse";
        String type = Biotech.MOD_ID + ":" + machineId;

        List<Crop> crops = CropData.CROPS;

        for (Crop crop : crops) {

            IngredientItemJsonObj[] ingredientItems = new IngredientItemJsonObj[]{
                    new IngredientItemJsonObj(new ItemStackJsonObj(crop.seedId(), 1), true)
            };
            FluidJsonObj[] fluidInputs = new FluidJsonObj[]{
                    new FluidJsonObj("minecraft:water", 200)
            };
            OutputItemJsonObj[] outputItems = crop.yields().stream()
                    .map(y -> new OutputItemJsonObj(new ItemStackJsonObj(y.id(), y.count()), y.chance())).toArray(OutputItemJsonObj[]::new);
            FluidJsonObj[] fluidOutputs = new FluidJsonObj[]{};
            int energy = 64000;

            String cropId =  crop.yields().get(0).id();
            String cropName = cropId.substring( cropId.indexOf(":") + 1);
            String recipeName = machineId + "_" + cropName;

            RecipeJson recipeJson = new RecipeJson(type, ingredientItems, outputItems, fluidInputs, fluidOutputs, energy);

            generateRecipe(recipeName, recipeJson);
        }
    }

    private void generateRecipe(String recipeName, RecipeJson recipeJson) {
        String json = GSON.toJson(recipeJson);

        Path outputPath = Paths.get(GEN_RECIPES_PATH, recipeName + ".json");
        try {
            Files.createDirectories(outputPath.getParent());
            LOGGER.info("Generating recipe " + recipeName);
            try (FileWriter writer = new FileWriter(outputPath.toFile())) {
                writer.write(json);
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to write recipe to file: " + outputPath);
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused", "ClassCanBeRecord"})
    private static class RecipeJson {
        private final String type;
        private final IngredientItemJsonObj[] itemInputs;
        private final OutputItemJsonObj[] itemOutputs;
        private final FluidJsonObj[] fluidInputs;
        private final FluidJsonObj[] fluidOutputs;
        private final int energy;

        public RecipeJson(String type,
                          IngredientItemJsonObj[] itemInputs,
                          OutputItemJsonObj[] itemOutputs,
                          FluidJsonObj[] fluidInputs,
                          FluidJsonObj[] fluidOutputs,
                          int energy) {
            this.type = type;
            this.itemInputs = itemInputs;
            this.itemOutputs = itemOutputs;
            this.fluidInputs = fluidInputs;
            this.fluidOutputs = fluidOutputs;
            this.energy = energy;
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static class ItemStackJsonObj {
        private final String id;
        private final int Count;

        public ItemStackJsonObj(String id, int count) {
            this.id = id;
            this.Count = count;
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused", "ClassCanBeRecord"})
    private static class IngredientItemJsonObj {
        private final ItemStackJsonObj itemStack;
        private final boolean isConsumable;

        public IngredientItemJsonObj(ItemStackJsonObj itemStack, boolean isConsumable) {
            this.itemStack = itemStack;
            this.isConsumable = isConsumable;
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused", "ClassCanBeRecord"})
    private static class OutputItemJsonObj {
        private final ItemStackJsonObj itemStack;
        private final float chance;

        public OutputItemJsonObj(ItemStackJsonObj itemStack, float chance) {
            this.itemStack = itemStack;
            this.chance = chance;
        }

        public OutputItemJsonObj(ItemStackJsonObj itemStack) {
            this.itemStack = itemStack;
            this.chance = 1.0f;
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static class FluidJsonObj {
        private final String FluidName;
        private final int Amount;

        public FluidJsonObj(String fluidName, int amount) {
            this.FluidName = fluidName;
            this.Amount = amount;
        }
    }
}
