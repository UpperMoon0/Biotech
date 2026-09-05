package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GreenhouseRuntimeStateTest {
    @Test
    void patternAvoidsNeighborAndRandomTickManagedStates() throws IOException {
        Path root = findRepositoryRoot();
        String source = Files.readString(root.resolve("neoforge-26.1.2/src/main/java/com/nstut/biotech/blocks/entites/machines/GreenhouseBlockEntity.java"));
        assertFalse(source.contains("\"moisture\", \"0\""));
        assertFalse(source.contains("\"shape\", \"straight\""));
        assertFalse(source.contains("\"east\", \"none\""));
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
