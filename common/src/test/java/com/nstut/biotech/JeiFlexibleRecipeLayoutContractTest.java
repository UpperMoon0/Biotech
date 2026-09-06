package com.nstut.biotech;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JeiFlexibleRecipeLayoutContractTest {
    private static final String[] TARGETS = {
            "forge-1.20.1",
            "neoforge-1.21.1",
            "neoforge-26.1.2"
    };

    @Test
    void allMachineLayoutsRespectGenericRecipeCardinality() throws IOException {
        Path root = findRepositoryRoot();
        for (String target : TARGETS) {
            assertFlexibleLayout(root, target, "FermenterCategory.java", false);
            assertFlexibleLayout(root, target, "MixerCategory.java", false);
            assertFlexibleLayout(root, target, "BreedingChamberCategory.java", true);
            assertFlexibleLayout(root, target, "TerrestrialHabitatCategory.java", true);
            assertFlexibleLayout(root, target, "SlaughterhouseCategory.java", true);
            assertFlexibleLayout(root, target, "GreenhouseCategory.java", true);
        }
    }

    private static void assertFlexibleLayout(Path root, String target, String category, boolean requireItemInputLoop) throws IOException {
        String source = Files.readString(root.resolve(target + "/src/main/java/com/nstut/biotech/jei/" + category));
        assertFalse(source.contains("getFluidIngredients().get(0)"), target + " " + category + " must allow zero/multiple fluid inputs");
        assertFalse(source.contains("getFluidOutputs().get(0)"), target + " " + category + " must render every fluid output");
        assertFalse(source.contains("getItemIngredients().get(0)"), target + " " + category + " must allow zero/multiple item inputs");
        assertFalse(source.contains("itemInputs.get(0)"), target + " " + category + " must not assume an item input exists");
        assertFalse(source.contains("ingredients.get(0)"), target + " " + category + " must not assume an item input exists");
        assertFalse(source.contains("itemOutputs.get(0)"), target + " " + category + " must render every item output");
        assertFalse(source.contains("outputs.get(0)"), target + " " + category + " must render every item output");
        assertTrue(source.contains("for (int i = 0; i < recipe.getFluidIngredients().size(); i++)"), target + " " + category);
        assertTrue(source.contains("for (int i = 0; i < recipe.getFluidOutputs().size(); i++)"), target + " " + category);
        assertTrue(source.contains("for (int i = 0; i < itemOutputs.size(); i++)"), target + " " + category);
        if (requireItemInputLoop) {
            assertTrue(source.contains("for (int i = 0; i < itemInputs.size(); i++)"), target + " " + category);
        }
    }

    private static Path findRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("gradle.properties"))
                    && Files.isRegularFile(current.resolve("settings.gradle"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate Biotech repository root");
    }
}
