package com.nstut.biotech;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivestockProcessingContractTest {
    private static final List<String> TARGETS = List.of(
            "forge-1.20.1", "neoforge-1.21.1", "neoforge-26.1.2");
    private static final List<String> CATEGORIES = List.of(
            "BreedingChamberCategory.java",
            "TerrestrialHabitatCategory.java",
            "SlaughterhouseCategory.java",
            "GreenhouseCategory.java",
            "FermenterCategory.java",
            "MixerCategory.java");

    @Test
    void everyMachineCategoryUsesSharedChanceRendering() throws IOException {
        Path root = findRepositoryRoot();
        for (String target : TARGETS) {
            String helper = Files.readString(root.resolve(target +
                    "/src/main/java/com/nstut/biotech/jei/JeiOutputChanceHelper.java"));
            assertTrue(helper.contains("chance >= 1.0f"), target + " must keep guaranteed outputs uncluttered");
            assertTrue(helper.contains("setScale(2, RoundingMode.HALF_UP)"),
                    target + " must render a stable exact percentage");

            for (String category : CATEGORIES) {
                String source = Files.readString(root.resolve(target +
                        "/src/main/java/com/nstut/biotech/jei/" + category));
                assertTrue(source.contains("JeiOutputChanceHelper.drawItemChances"),
                        target + " " + category + " must use the shared chance renderer");
            }
        }
    }

    @Test
    void slaughterhouseNoLongerHasHardCodedAnimalDrops() throws IOException {
        Path root = findRepositoryRoot();
        String creatures = Files.readString(root.resolve(
                "common/src/main/java/com/nstut/biotech/data/CreatureData.java"));
        assertFalse(creatures.contains("DROPS"), "entity loot tables must be the Slaughterhouse source of truth");

        for (String target : List.of("forge-1.20.1", "common-neoforge")) {
            String generator = Files.readString(root.resolve(target +
                    "/src/main/java/com/nstut/biotech/data/RecipeGenerator.java"));
            assertFalse(generator.contains("CreatureData.DROPS"), target);
            assertTrue(generator.contains("Runtime preparation resolves the concrete captured entity's loot table exactly once"),
                    target);
        }
    }

    @Test
    void terrestrialHabitatShipsRenewableItemAndFluidProduction() throws IOException {
        Path root = findRepositoryRoot();
        for (String target : List.of("forge-1.20.1", "common-neoforge")) {
            String generator = Files.readString(root.resolve(target +
                    "/src/main/java/com/nstut/biotech/data/RecipeGenerator.java"));
            assertTrue(generator.contains("CREATURE_CHICKEN, \"eggs\", \"minecraft:egg\""), target);
            assertTrue(generator.contains("CREATURE_SHEEP, \"wool\", \"minecraft:white_wool\""), target);
            assertTrue(generator.contains("generateRenewableHabitatMilkRecipes"), target);
            assertTrue(generator.contains("new IngredientItemJsonObj(new ItemStackJsonObj(creature.id(), 1), false)"),
                    target + " adult animal must be a non-consumable catalyst");
        }

        for (String target : List.of("forge-1.20.1", "common-neoforge")) {
            String machine = Files.readString(root.resolve(target +
                    "/src/main/java/com/nstut/biotech/blocks/entites/machines/TerrestrialHabitatBlockEntity.java"));
            assertTrue(machine.contains("FluidOutputHatchBlockEntity"), target);
            assertTrue(machine.contains("List.of(outputFluid)"), target);
            assertTrue(machine.contains("prepareHabitat(recipe, inputItems)"), target);
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
