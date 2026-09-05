package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Port121RegressionTest {
    @Test
    void greenhouseDoesNotRequireRuntimeDerivedBlockStates() throws IOException {
        Path root = findRepositoryRoot();
        String greenhouse = Files.readString(root.resolve("neoforge-1.21.1/src/main/java/com/nstut/biotech/blocks/entites/machines/GreenhouseBlockEntity.java"));

        assertFalse(greenhouse.contains("Map.of(\"moisture\", \"0\")"), "farmland moisture changes naturally beside the greenhouse water source");
        assertFalse(greenhouse.contains("\"shape\", \"straight\""), "stair shape is recomputed from neighboring stairs");
        assertFalse(greenhouse.contains("\"east\", \"none\", \"waterlogged\""), "wall connection states are recomputed from neighbors");
    }

    @Test
    void modernRecipeResourcesUseTheSingularRegistryDirectory() {
        Path root = findRepositoryRoot();
        assertTrue(Files.isRegularFile(root.resolve("neoforge-1.21.1/src/generated/resources/data/biotech/recipe/greenhouse_wheat.json")));
        assertFalse(Files.exists(root.resolve("neoforge-1.21.1/src/generated/resources/data/biotech/recipes")));
        assertTrue(Files.isRegularFile(root.resolve("neoforge-1.21.1/src/main/resources/data/biotech/recipe/greenhouse.json")));
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
