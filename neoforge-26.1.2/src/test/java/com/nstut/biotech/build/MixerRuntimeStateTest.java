package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixerRuntimeStateTest {
    @Test
    void patternAvoidsNeighborManagedStairShape() throws IOException {
        Path root = findRepositoryRoot();
        String source = Files.readString(root.resolve("common-neoforge/src/main/java/com/nstut/biotech/blocks/entites/machines/MixerBlockEntity.java"));

        assertFalse(source.contains("\"shape\","));
        assertTrue(source.contains("\"half\", \"bottom\""));
        assertTrue(source.contains("\"half\", \"top\""));
        assertTrue(source.contains("\"waterlogged\", \"false\""));
        assertTrue(source.contains("\"facing\", \"west\""));
    }

    private static Path findRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("gradle.properties")) && Files.isRegularFile(current.resolve("settings.gradle"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate Biotech repository root");
    }
}
