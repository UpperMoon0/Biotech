package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GreenhouseRuntimeStateTest {
    @Test
    void patternAvoidsNeighborAndRandomTickManagedStates() throws IOException {
        String source = Files.readString(Path.of(
                "src", "main", "java", "com", "nstut", "biotech", "blocks", "entites", "machines", "GreenhouseBlockEntity.java"));
        assertFalse(source.contains("\"moisture\", \"0\""));
        assertFalse(source.contains("\"shape\", \"straight\""));
        assertFalse(source.contains("\"east\", \"none\""));
    }
}
