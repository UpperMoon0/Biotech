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
        Path repositoryRoot = findRepositoryRoot(Path.of(System.getProperty("user.dir")).toAbsolutePath());

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(repositoryRoot.resolve("gradle.properties"))) {
            properties.load(reader);
        }

        assertEquals("1.21.1", properties.getProperty("minecraft_version_1_21_1"));
        assertEquals("[1.21.1,1.22)", properties.getProperty("minecraft_version_range_1_21_1"));
        assertEquals("21.1.240", properties.getProperty("neoforge_version_1_21_1"));
        assertEquals("0.8.1", properties.getProperty("nstut_lib_version"));
        assertEquals("e8386ae2528289888ced1b46cd8728f102552d43", properties.getProperty("nstut_lib_ref"));

        String metadataTemplate = Files.readString(repositoryRoot.resolve(
                Path.of("neoforge-1.21.1", "src/main/templates/META-INF/neoforge.mods.toml")));
        assertTrue(metadataTemplate.contains("versionRange = \"[0.8.1,0.9)\""));
    }

    private static Path findRepositoryRoot(Path start) throws IOException {
        for (Path current = start; current != null; current = current.getParent()) {
            if (Files.isRegularFile(current.resolve("gradle.properties"))
                    && Files.isRegularFile(current.resolve(
                    Path.of("neoforge-1.21.1", "src/main/templates/META-INF/neoforge.mods.toml")))) {
                return current;
            }
        }
        throw new IOException("Unable to locate Biotech repository root from " + start);
    }
}
