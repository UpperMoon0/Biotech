package com.nstut.biotech;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CapturedAnimalRecipeContractTest {
    @Test
    void animalRecipesShareCapturedMobMatchingPolicy() throws IOException {
        Path root = findRepositoryRoot();
        String legacyHelper = Files.readString(root.resolve("common-legacy/src/main/java/com/nstut/biotech/recipes/AnimalMobRecipe.java"));
        String modernHelper = Files.readString(root.resolve("neoforge-26.1.2/src/main/java/com/nstut/biotech/recipes/AnimalMobRecipe.java"));
        assertMatchingPolicy(legacyHelper, "common-legacy");
        assertMatchingPolicy(modernHelper, "neoforge-26.1.2");

        for (String recipe : new String[] {"BreedingChamberRecipe.java", "TerrestrialHabitatRecipe.java", "SlaughterhouseRecipe.java"}) {
            String legacyRecipe = Files.readString(root.resolve("common-legacy/src/main/java/com/nstut/biotech/recipes/" + recipe));
            String modernRecipe = Files.readString(root.resolve("neoforge-26.1.2/src/main/java/com/nstut/biotech/recipes/" + recipe));
            assertTrue(legacyRecipe.contains("extends AnimalMobRecipe"), "common-legacy " + recipe);
            assertTrue(modernRecipe.contains("extends AnimalMobRecipe"), "neoforge-26.1.2 " + recipe);
        }
    }

    @Test
    void genericCarrierBridgePreservesAdultAndBabyVariants() throws IOException {
        Path root = findRepositoryRoot();
        for (String target : new String[] {"forge-1.20.1", "neoforge-1.21.1", "neoforge-26.1.2"}) {
            String mobItem = Files.readString(root.resolve(target + "/src/main/java/com/nstut/biotech/items/MobItem.java"));
            String captured = Files.readString(root.resolve(target + "/src/main/java/com/nstut/biotech/items/CapturedAnimalItem.java"));

            assertTrue(mobItem.contains("public EntityType<? extends Mob> entityType()"), target);
            assertTrue(mobItem.contains("public boolean isBabyVariant()"), target);
            assertTrue(captured.contains("matchesLegacyVariant"), target);
            assertTrue(captured.contains("matchesGenericSpecies"), target);
            assertTrue(captured.contains("requiredType == presentType"), target);
            assertTrue(captured.contains("actualType != expectedType"), target);
            assertTrue(captured.contains("actualBaby == expectedBaby"), target);
            assertTrue(captured.contains("\"Age\""), target);
        }
    }

    private static void assertMatchingPolicy(String source, String target) {
        assertTrue(source.contains("required.getItem() instanceof MobItem"), target);
        assertTrue(source.contains("present.getItem() instanceof MobItem"), target);
        assertTrue(source.contains("required.is(present.getItem())"), target);
        assertTrue(source.contains("present.getItem() instanceof CapturedAnimalItem"), target);
        assertTrue(source.contains("requiredMob.entityType()"), target);
        assertTrue(source.contains("requiredMob.isBabyVariant()"), target);
        assertTrue(source.contains("CapturedAnimalItem.matchesLegacyVariant"), target);
        assertTrue(source.contains("CapturedAnimalItem.matchesGenericSpecies(required, present)"), target);
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
