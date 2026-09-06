package com.nstut.biotech;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CapturedAnimalRecipeContractTest {
    @Test
    void animalRecipesShareNarrowCapturedMobMatchingPolicy() throws IOException {
        Path root = findRepositoryRoot();
        String legacyHelper = Files.readString(root.resolve("common-legacy/src/main/java/com/nstut/biotech/recipes/AnimalMobRecipe.java"));
        String modernHelper = Files.readString(root.resolve("neoforge-26.1.2/src/main/java/com/nstut/biotech/recipes/AnimalMobRecipe.java"));
        assertNarrowPolicy(legacyHelper, "common-legacy");
        assertNarrowPolicy(modernHelper, "neoforge-26.1.2");

        for (String recipe : new String[] {"BreedingChamberRecipe.java", "TerrestrialHabitatRecipe.java", "SlaughterhouseRecipe.java"}) {
            String legacyRecipe = Files.readString(root.resolve("common-legacy/src/main/java/com/nstut/biotech/recipes/" + recipe));
            String modernRecipe = Files.readString(root.resolve("neoforge-26.1.2/src/main/java/com/nstut/biotech/recipes/" + recipe));
            assertTrue(legacyRecipe.contains("extends AnimalMobRecipe"), "common-legacy " + recipe);
            assertTrue(modernRecipe.contains("extends AnimalMobRecipe"), "neoforge-26.1.2 " + recipe);
        }
    }

    private static void assertNarrowPolicy(String source, String target) {
        assertTrue(source.contains("required.getItem() instanceof MobItem"), target);
        assertTrue(source.contains("present.getItem() instanceof MobItem"), target);
        assertTrue(source.contains("required.is(present.getItem())"), target);
        assertTrue(source.contains("return super.itemIngredientsMatch(required, present)"), target);
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
