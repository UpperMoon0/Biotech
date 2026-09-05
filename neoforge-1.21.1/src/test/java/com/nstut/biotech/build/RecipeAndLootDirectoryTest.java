package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeAndLootDirectoryTest {
    @Test
    void modernDirectoriesExist() {
        Path root = findRepositoryRoot();
        Path data = root.resolve("neoforge-1.21.1/src/generated/resources/data/biotech");
        assertTrue(Files.isDirectory(data.resolve("recipe")));
        assertTrue(Files.isDirectory(data.resolve("loot_table")));
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
