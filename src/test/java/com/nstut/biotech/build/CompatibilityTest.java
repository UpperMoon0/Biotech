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
        try (Reader reader = Files.newBufferedReader(Path.of("gradle.properties"))) {
            properties.load(reader);
        }

        assertEquals("1.20.1", properties.getProperty("minecraft_version"));
        assertEquals("47.3.12", properties.getProperty("forge_version"));
        assertEquals("[47.3.12,48)", properties.getProperty("forge_version_range"));
        assertEquals("[47,48)", properties.getProperty("loader_version_range"));
        assertEquals("06d493ee46ad8465a391c8291fa10abca2c4ff4e", properties.getProperty("nstut_lib_version"));

        String modsToml = Files.readString(Path.of("src/main/resources/META-INF/mods.toml"));
        assertTrue(modsToml.contains("versionRange=\"[0.8,0.9)\""));
    }
}
