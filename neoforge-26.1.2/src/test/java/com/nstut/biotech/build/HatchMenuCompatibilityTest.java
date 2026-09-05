package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HatchMenuCompatibilityTest {
    @Test
    void resourceBackedHatchMenusUseNativeResourceHandlerSlots() throws IOException {
        Path repositoryRoot = findRepositoryRoot();
        List<Path> menus = List.of(
                Path.of("neoforge-26.1.2", "src", "main", "java", "com", "nstut", "biotech", "views", "io_hatches", "item", "ItemHatchMenu.java"),
                Path.of("neoforge-26.1.2", "src", "main", "java", "com", "nstut", "biotech", "views", "io_hatches", "fluid", "FluidHatchMenu.java")
        );

        for (Path menu : menus) {
            String source = Files.readString(repositoryRoot.resolve(menu));
            assertTrue(source.contains("ResourceHandlerSlot"), menu + " must use NeoForge 26 native resource slots");
            assertFalse(source.contains("SlotItemHandler"), menu + " must not use the legacy slot that casts adapters to IItemHandlerModifiable");
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
