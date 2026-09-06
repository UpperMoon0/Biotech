package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Port261RegressionTest {
    @Test
    void greenhouseDoesNotRequireRuntimeDerivedBlockStates() throws IOException {
        Path root = findRepositoryRoot();
        String greenhouse = Files.readString(root.resolve("common-neoforge/src/main/java/com/nstut/biotech/blocks/entites/machines/GreenhouseBlockEntity.java"));

        assertFalse(greenhouse.contains("Map.of(\"moisture\", \"0\")"));
        assertFalse(greenhouse.contains("\"shape\", \"straight\""));
        assertFalse(greenhouse.contains("\"east\", \"none\", \"waterlogged\""));
    }

    @Test
    void itemOutputCapabilityKeepsGeneralCapacityForExtractionAutomation() throws IOException {
        Path root = findRepositoryRoot();
        String itemHatch = Files.readString(root.resolve("neoforge-26.1.2/src/main/java/com/nstut/biotech/blocks/entites/hatches/ItemHatchBlockEntity.java"));

        assertTrue(itemHatch.contains("if (resource.isEmpty())"));
        assertTrue(itemHatch.contains("return slots.getCapacityAsLong(index, resource);"));
        assertTrue(itemHatch.contains("return isInputHatch() ? slots.getCapacityAsLong(index, resource) : 0;"));
        assertTrue(itemHatch.contains("return isInputHatch() ? slots.insert(index, resource, amount, transaction) : 0;"));
        assertTrue(itemHatch.contains("return isInputHatch() ? 0 : slots.extract(index, resource, amount, transaction);"));
    }

    @Test
    void generatedMachineRecipesUseTheSingularRegistryDirectory() {
        Path root = findRepositoryRoot();
        assertTrue(Files.isRegularFile(root.resolve("neoforge-26.1.2/src/generated/resources/data/biotech/recipe/greenhouse_wheat.json")));
        assertFalse(Files.exists(root.resolve("neoforge-26.1.2/src/generated/resources/data/biotech/recipes")));
    }

    private static Path findRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("gradle.properties")) && Files.isRegularFile(current.resolve("settings.gradle"))) return current;
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate Biotech repository root");
    }
}
