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
        assertEquals("2eba051973e967a2fbcee2bf1a5fef48554540f8", properties.getProperty("nstut_lib_ref"));

        String modsToml = Files.readString(Path.of("src", "main", "resources", "META-INF", "mods.toml"));
        assertTrue(modsToml.contains("versionRange=\"[0.8.2,0.9)\""));
    }
}
