package com.nstut.biotech;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JeiSpriteFreeLayoutContractTest {
    private static final List<String> TARGETS = List.of(
            "forge-1.20.1", "neoforge-1.21.1", "neoforge-26.1.2");
    private static final List<String> CATEGORIES = List.of(
            "BreedingChamberCategory.java", "TerrestrialHabitatCategory.java",
            "SlaughterhouseCategory.java", "GreenhouseCategory.java",
            "FermenterCategory.java", "MixerCategory.java");

    @Test
    void machineJeiUsesJeiPrimitivesInsteadOfCustomSprites() throws IOException {
        Path root = findRepositoryRoot();
        Path spriteDir = root.resolve("common/src/main/resources/assets/biotech/textures/gui/jei");
        assertFalse(Files.exists(spriteDir) && hasPng(spriteDir),
                "JEI machine cards must not ship custom background sprites");

        for (String target : TARGETS) {
            for (String category : CATEGORIES) {
                String source = Files.readString(root.resolve(target +
                        "/src/main/java/com/nstut/biotech/jei/" + category));
                String context = target + " " + category;
                assertFalse(source.contains("textures/gui/jei/"), context);
                assertFalse(source.contains("createDrawable(TEXTURE"), context);
                assertFalse(source.contains("createBlankDrawable"), context);
                assertFalse(source.contains("getSlotDrawable"), context);
                assertFalse(source.contains("JeiCategoryDraw"), context);
                assertTrue(source.contains("createDrawableItemLike"), context);
                assertTrue(source.contains("getRecipeArrow"), context);
                assertTrue(source.contains("setStandardSlotBackground"), context);
                assertTrue(source.contains("getWidth()"), context);
                assertTrue(source.contains("getHeight()"), context);
            }
        }
    }

    private static boolean hasPng(Path dir) throws IOException {
        try (var paths = Files.walk(dir)) {
            return paths.anyMatch(path -> Files.isRegularFile(path) &&
                    path.getFileName().toString().endsWith(".png"));
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
