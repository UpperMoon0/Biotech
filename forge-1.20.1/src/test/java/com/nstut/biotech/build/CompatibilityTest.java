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
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of("..", "gradle.properties"))) {
            properties.load(reader);
        }

        assertEquals("1.20.1", properties.getProperty("minecraft_version_1_20_1"));
        assertEquals("47.3.12", properties.getProperty("forge_version_1_20_1"));
        assertEquals("[47.3.12,48)", properties.getProperty("forge_version_range_1_20_1"));
        assertEquals("[47,48)", properties.getProperty("forge_loader_range_1_20_1"));
        assertEquals("0.8.2", properties.getProperty("nstut_lib_version"));
        assertEquals("0.0.16", properties.getProperty("openui_version"));
        assertEquals("40bb62363c705bd18221ef02f0e3e5605777e28c", properties.getProperty("openui_ref"));
        assertEquals("bcf7a86bfc47c71810c70ff0424220c4ea0693c9", properties.getProperty("nstut_lib_ref"));

        String modsToml = Files.readString(Path.of("src", "main", "resources", "META-INF", "mods.toml"));
        assertTrue(modsToml.contains("versionRange=\"[0.8.2,)\""));
        assertTrue(modsToml.contains("versionRange=\"[0.0.16,)\""));
    }
}
