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
        assertEquals("26.1.2.99", properties.getProperty("neoforge_version_26_1_2"));
        assertEquals("0.8.2", properties.getProperty("nstut_lib_version"));
        assertEquals("0.0.16", properties.getProperty("openui_version"));
        assertEquals("40bb62363c705bd18221ef02f0e3e5605777e28c", properties.getProperty("openui_ref"));
        assertEquals("bcf7a86bfc47c71810c70ff0424220c4ea0693c9", properties.getProperty("nstut_lib_ref"));

        String metadataTemplate = Files.readString(repositoryRoot.resolve(Path.of("neoforge-26.1.2", "src/main/templates/META-INF/neoforge.mods.toml")));
        assertTrue(metadataTemplate.contains("versionRange = \"[0.8.2,)\""));
        assertTrue(metadataTemplate.contains("versionRange = \"[0.0.16,)\""));
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
