package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilityTest {
    @Test
    void hardeningReleasePinsValidatedDependencyAndCompatibilityBounds() throws IOException {
        Path repositoryRoot = findRepositoryRoot();
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(repositoryRoot.resolve("gradle.properties"))) {
            properties.load(reader);
        }

        assertEquals("26.1.2", properties.getProperty("minecraft_version_26_1_2"));
        assertEquals("[26.1.2,26.2)", properties.getProperty("minecraft_version_range_26_1_2"));
        assertEquals("26.1.2.76", properties.getProperty("neoforge_version_26_1_2"));
        assertEquals("0.8", properties.getProperty("nstut_lib_version"));
        assertEquals("c4eb4005abd2d8fb7d20efab625570896aee7099", properties.getProperty("nstut_lib_ref"));

        String metadataTemplate = Files.readString(repositoryRoot.resolve(Path.of("neoforge-26.1.2", "src/main/templates/META-INF/neoforge.mods.toml")));
        assertTrue(metadataTemplate.contains("versionRange = \"[0.8,0.9)\""));
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
