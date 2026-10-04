package com.nstut.biotech;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Invokes the real generator while game/loader classes are unavailable to it. */
class RegistryFreeProductionDatagenTest {
    @Test
    void renewableGenerationDoesNotInitializeMinecraftOrLoaderRegistries() throws Exception {
        ClassLoader generatorOnly = new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("net.minecraft") || name.startsWith("net.neoforged")
                        || name.equals("com.nstut.biotech.Biotech")) {
                    throw new ClassNotFoundException("Standalone datagen cannot load game/loader class " + name);
                }
                if (name.startsWith("com.nstut.biotech.data.") || name.startsWith("com.nstut.biotech.models.")) {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> loaded = findLoadedClass(name);
                        if (loaded == null) {
                            try (var stream = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                                if (stream == null) throw new ClassNotFoundException(name);
                                byte[] bytes = stream.readAllBytes();
                                loaded = defineClass(name, bytes, 0, bytes.length);
                            } catch (IOException exception) {
                                throw new ClassNotFoundException(name, exception);
                            }
                        }
                        if (resolve) resolveClass(loaded);
                        return loaded;
                    }
                }
                return super.loadClass(name, resolve);
            }
        };
        // Determine the target before DataGenerator initializes its static directory constants.
        boolean legacy = Path.of("").toAbsolutePath().getFileName().toString().equals("forge-1.20.1");
        String previous = System.getProperty("biotech.recipeDir");
        try {
            System.setProperty("biotech.recipeDir", legacy ? "recipes" : "recipe");
            Class<?> generator = generatorOnly.loadClass("com.nstut.biotech.data.RecipeGenerator");
            generator.getMethod("generate").invoke(generator.getConstructor().newInstance());
            Path recipes = Path.of("src/generated/resources/data/biotech", legacy ? "recipes" : "recipe");
            JsonObject milk = read(recipes.resolve("terrestrial_habitat_cow_milk_t1_wheat.json"));
            assertFalse(milk.getAsJsonArray("itemInputs").get(0).getAsJsonObject().get("isConsumable").getAsBoolean());
            JsonObject fluid = milk.getAsJsonArray("fluidOutputs").get(0).getAsJsonObject();
            assertEquals("minecraft:milk", fluid.get(legacy ? "FluidName" : "id").getAsString());
            assertEquals(1000, fluid.get(legacy ? "Amount" : "amount").getAsInt());
            assertEquals(32000, milk.get("energy").getAsInt());
            assertEquals(0, read(recipes.resolve("slaughterhouse_cow.json")).getAsJsonArray("itemOutputs").size());
        } finally {
            if (previous == null) System.clearProperty("biotech.recipeDir"); else System.setProperty("biotech.recipeDir", previous);
        }
    }

    private static JsonObject read(Path file) throws IOException {
        try (var reader = Files.newBufferedReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
