package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModernResourceDirectoryTest {
    @Test
    void generatedRecipesAndLootTablesUseSingularDirectories() {
        Path root = findRepositoryRoot();
        Path generated = root.resolve("neoforge-1.21.1/src/generated/resources/data/biotech");
        assertTrue(Files.isDirectory(generated.resolve("recipe")));
        assertTrue(Files.isDirectory(generated.resolve("loot_table")));
        assertFalse(Files.exists(generated.resolve("recipes")));
        assertFalse(Files.exists(generated.resolve("loot_tables")));
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
