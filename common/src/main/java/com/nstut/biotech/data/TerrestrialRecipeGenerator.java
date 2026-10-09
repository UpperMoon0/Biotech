package com.nstut.biotech.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;

/** Generic captured selectors preserve concrete state while keeping version-correct stack schemas. */
public final class TerrestrialRecipeGenerator extends DataGenerator {
    @Override protected void generate() {
        int version = Integer.getInteger("biotech.animalVersion", 121);
        for (var animal : TerrestrialAnimalCatalog.extras(version)) {
            var slaughter = recipe("slaughterhouse", 16000, 200);
            input(slaughter, captured(animal.id(), "adult", 1), true);
            write("slaughterhouse_" + animal.id(), slaughter);

            var production = recipe("terrestrial_habitat", 32000, 250);
            input(production, captured(animal.id(), "adult", 1), false);
            food(production, animal.food(), 2);
            output(production, stack("biotech:manure", 1));
            if (animal.id().equals("goat") || animal.id().equals("mooshroom")) {
                var milk = new JsonObject(); milk.addProperty(legacy() ? "FluidName" : "id", "minecraft:milk"); milk.addProperty(legacy() ? "Amount" : "amount", 1000);
                production.getAsJsonArray("fluidOutputs").add(milk);
            }
            write("terrestrial_habitat_" + animal.id() + "_renewable", production);

            if (animal.baby()) {
                var growth = recipe("terrestrial_habitat", 48000, 400);
                input(growth, captured(animal.id(), "baby", 1), true);
                food(growth, animal.food(), 4);
                output(growth, captured(animal.id(), "adult", 1));
                output(growth, stack("biotech:manure", 2));
                if (animal.id().equals("turtle")) output(growth, stack(version >= 121 ? "minecraft:turtle_scute" : "minecraft:scute", 1));
                write("terrestrial_habitat_" + animal.id() + "_growth", growth);
            }
            if (animal.breeds()) {
                var breeding = recipe("breeding_chamber", 20000, 200);
                input(breeding, captured(animal.id(), "adult", 1), false);
                input(breeding, captured(animal.id(), "adult", 1), false);
                food(breeding, animal.food(), 2);
                output(breeding, animal.egg().isEmpty() ? captured(animal.id().equals("trader_llama") ? "llama" : animal.id(), "baby", 1)
                        : stack("minecraft:" + animal.egg(), 1));
                write("breeding_chamber_" + animal.id(), breeding);
            }
        }
        var hybrid = recipe("breeding_chamber", 20000, 200);
        input(hybrid, captured("horse", "adult", 1), false);
        input(hybrid, captured("donkey", "adult", 1), false);
        food(hybrid, "golden_carrot", 2);
        output(hybrid, captured("mule", "baby", 1));
        write("breeding_chamber_horse_donkey", hybrid);
    }
    private static JsonObject stack(String id, int count) {
        var stack = new JsonObject(); stack.addProperty("id", id);
        stack.addProperty(legacy() ? "Count" : "count", count); return stack;
    }
    private static boolean legacy() { return System.getProperty("biotech.recipeDir", "recipe").equals("recipes"); }
    private static JsonObject captured(String type, String age, int count) {
        var stack = stack("biotech:captured_animal", count);
        var data = new JsonObject(); data.addProperty("EntityType", "minecraft:" + type);
        data.addProperty("BiotechRecipeLifecycle", age);
        if (legacy()) stack.add("tag", data);
        else { var components = new JsonObject(); components.add("minecraft:custom_data", data); stack.add("components", components); }
        return stack;
    }
    private static JsonObject recipe(String type, int energy, int water) {
        var recipe = new JsonObject(); recipe.addProperty("type", "biotech:" + type); recipe.addProperty("energy", energy);
        for (String field : new String[]{"itemInputs", "itemOutputs", "fluidInputs", "fluidOutputs"}) recipe.add(field, new JsonArray());
        var fluid = new JsonObject(); fluid.addProperty(legacy() ? "FluidName" : "id", "minecraft:water");
        fluid.addProperty(legacy() ? "Amount" : "amount", water); recipe.getAsJsonArray("fluidInputs").add(fluid);
        return recipe;
    }
    private static void food(JsonObject recipe, String food, int count) { if (!food.isEmpty()) input(recipe, stack("minecraft:" + food, count), true); }
    private static void input(JsonObject recipe, JsonObject stack, boolean consumed) {
        var entry = new JsonObject(); entry.add("itemStack", stack); entry.addProperty("isConsumable", consumed);
        recipe.getAsJsonArray("itemInputs").add(entry);
    }
    private static void output(JsonObject recipe, JsonObject stack) {
        var entry = new JsonObject(); entry.add("itemStack", stack); entry.addProperty("chance", 1.0);
        recipe.getAsJsonArray("itemOutputs").add(entry);
    }
    private static void write(String name, JsonObject recipe) {
        try { Files.writeString(Path.of(GEN_RECIPES_PATH, name + ".json"), new GsonBuilder().setPrettyPrinting().create().toJson(recipe)); }
        catch (java.io.IOException failure) { throw new IllegalStateException("Could not write animal recipe " + name, failure); }
    }
}
