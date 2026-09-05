package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModernCraftingRecipePresenceTest {
    @Test
    void greenhouseCraftingRecipeUsesSingularDirectory() {
        Path root = findRepositoryRoot();
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
