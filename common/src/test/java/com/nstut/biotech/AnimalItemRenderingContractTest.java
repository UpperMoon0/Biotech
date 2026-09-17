package com.nstut.biotech;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimalItemRenderingContractTest {
    private static final List<String> ANIMAL_ITEMS = List.of(
            "cow", "baby_cow",
            "chicken", "baby_chicken",
            "pig", "baby_pig",
            "sheep", "baby_sheep",
            "rabbit", "baby_rabbit",
            "captured_animal");

    @Test
    void legacyTargetsUseBuiltinEntityModelsAndNoLegacyAnimalTextures() throws IOException {
        Path root = findRepositoryRoot();
        Path models = root.resolve("common/src/main/resources/assets/biotech/models/item");
        String base = Files.readString(models.resolve("animal_model.json"));
        assertTrue(base.contains("\"parent\": \"builtin/entity\""));

        for (String item : ANIMAL_ITEMS) {
            String json = Files.readString(models.resolve(item + ".json"));
            assertTrue(json.contains("\"parent\": \"biotech:item/animal_model\""), item);
        }

        Path textures = root.resolve("common/src/main/resources/assets/biotech/textures/item");
        for (String texture : List.of("cow.png", "chicken.png", "pig.png", "sheep.png", "rabbit.png", "baby_base.png")) {
            assertFalse(Files.exists(textures.resolve(texture)), "obsolete animal texture must be removed: " + texture);
        }

        for (String target : List.of("forge-1.20.1", "neoforge-1.21.1")) {
            String renderer = Files.readString(root.resolve(target + "/src/main/java/com/nstut/biotech/client/AnimalItemRenderer.java"));
            assertTrue(renderer.contains("EntityRenderDispatcher"), target);
            assertTrue(renderer.contains("entityRenderer.render"), target);
            assertTrue(renderer.contains("createCapturedEntity"), target);
            assertTrue(renderer.contains("createMob"), target);
        }
    }

    @Test
    void modernTargetUsesRegisteredSpecialEntityRenderer() throws IOException {
        Path root = findRepositoryRoot();
        Path items = root.resolve("neoforge-26.1.2/src/main/resources/assets/biotech/items");
        for (String item : ANIMAL_ITEMS) {
            String json = Files.readString(items.resolve(item + ".json"));
            assertTrue(json.contains("\"type\": \"minecraft:special\""), item);
            assertTrue(json.contains("\"type\": \"biotech:animal\""), item);
        }

        String renderer = Files.readString(root.resolve(
                "neoforge-26.1.2/src/main/java/com/nstut/biotech/client/AnimalSpecialRenderer.java"));
        assertTrue(renderer.contains("dispatcher.extractEntity"));
        assertTrue(renderer.contains("dispatcher.submit"));
        assertTrue(renderer.contains("CapturedEntityState.sanitize"));

        String bootstrap = Files.readString(root.resolve(
                "neoforge-26.1.2/src/main/java/com/nstut/biotech/Biotech.java"));
        assertTrue(bootstrap.contains("RegisterSpecialModelRendererEvent"));
        assertTrue(bootstrap.contains("AnimalSpecialRenderer.Unbaked.MAP_CODEC"));
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
