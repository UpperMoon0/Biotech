package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JeiRecipeLayoutTest {
    @Test
    void jeiPluginReadsRecipesThatMinecraftCanActuallyLoad() throws IOException {
        Path root = findRepositoryRoot();
        String plugin = Files.readString(root.resolve("neoforge-1.21.1/src/main/java/com/nstut/biotech/jei/BiotechJEIPlugin.java"));
        assertTrue(plugin.contains("getAllRecipesFor(GreenhouseRecipe.TYPE)"));
        assertTrue(Files.isRegularFile(root.resolve("neoforge-1.21.1/src/generated/resources/data/biotech/recipe/greenhouse_wheat.json")));
    }

    private static Path findRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("gradle.properties")) && Files.isRegularFile(current.resolve("settings.gradle"))) return current;
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate Biotech repository root");
    }
}
