package com.nstut.biotech;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NetTrapCaptureContractTest {
    private static final String[] TARGETS = {
            "forge-1.20.1",
            "neoforge-1.21.1",
            "neoforge-26.1.2"
    };

    private static final List<String> DEFAULT_CAPTURABLES = List.of(
            "minecraft:cow",
            "minecraft:chicken",
            "minecraft:pig",
            "minecraft:sheep",
            "minecraft:rabbit",
            "minecraft:horse",
            "minecraft:goat",
            "minecraft:llama",
            "minecraft:camel"
    );

    @Test
    void netTrapUsesDataDrivenCapturabilityWithLegacyCompatibilityBridge() throws IOException {
        Path root = findRepositoryRoot();
        for (String target : TARGETS) {
            String source = Files.readString(root.resolve(target + "/src/main/java/com/nstut/biotech/blocks/NetTrapBlock.java"));

            assertTrue(source.contains("entity.getType().is(CAPTURABLE)"), target + " must gate capture through the entity tag");
            assertTrue(source.contains("CapturedAnimalItem.ENTITY_TYPE_TAG"), target + " must persist the captured entity type");
            assertTrue(source.contains("ItemRegistries.CAPTURED_ANIMAL"), target + " must fall back to the generic carrier");

            // The old five item identities intentionally remain as a migration bridge for existing recipes/worlds.
            assertTrue(source.contains("entity.getType() == EntityType.COW"), target);
            assertTrue(source.contains("entity.getType() == EntityType.CHICKEN"), target);
            assertTrue(source.contains("entity.getType() == EntityType.PIG"), target);
            assertTrue(source.contains("entity.getType() == EntityType.SHEEP"), target);
            assertTrue(source.contains("entity.getType() == EntityType.RABBIT"), target);
        }
    }

    @Test
    void capturableTagsIncludeLegacyAndExpandedAnimals() throws IOException {
        Path root = findRepositoryRoot();
        for (String target : TARGETS) {
            String tagDirectory = target.equals("forge-1.20.1") ? "entity_types" : "entity_type";
            Path tag = root.resolve(target + "/src/main/resources/data/biotech/tags/" + tagDirectory + "/capturable.json");
            String json = Files.readString(tag);

            for (String entityId : DEFAULT_CAPTURABLES) {
                assertTrue(json.contains("\"" + entityId + "\""), target + " missing " + entityId);
            }
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
