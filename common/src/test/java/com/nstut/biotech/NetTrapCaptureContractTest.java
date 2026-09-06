package com.nstut.biotech;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NetTrapCaptureContractTest {
    private static final String[] TARGETS = {
            "forge-1.20.1",
            "neoforge-1.21.1",
            "neoforge-26.1.2"
    };

    @Test
    void netTrapCapturesOnlyExactSupportedEntityTypes() throws IOException {
        Path root = findRepositoryRoot();
        for (String target : TARGETS) {
            String source = Files.readString(root.resolve(target + "/src/main/java/com/nstut/biotech/blocks/NetTrapBlock.java"));
            assertTrue(source.contains("entity.getType() == EntityType.COW && entity instanceof Cow cow"), target);
            assertTrue(source.contains("entity.getType() == EntityType.CHICKEN && entity instanceof Chicken chicken"), target);
            assertTrue(source.contains("entity.getType() == EntityType.PIG && entity instanceof Pig pig"), target);
            assertTrue(source.contains("entity.getType() == EntityType.SHEEP && entity instanceof Sheep sheep"), target);
            assertTrue(source.contains("entity.getType() == EntityType.RABBIT && entity instanceof Rabbit rabbit"), target);
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
