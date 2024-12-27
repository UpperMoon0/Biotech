package com.nstut.biotech.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MachineGenerator extends DataGenerator {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Logger LOGGER = Logger.getLogger(MachineGenerator.class.getName());

    @Override
    public void generate() {
        List<String> machineIds = List.of(
            "breeding_chamber",
            "terrestrial_habitat",
            "slaughterhouse",
            "greenhouse",
            "fermenter"
        );

        for (String machineId : machineIds) {
            generateMachine(machineId);
        }
    }

    private void generateMachine(String machineId) {
        generateTexture(machineId);
        generateModels(machineId);
        generateBlockstate(machineId);
        generateLootTable(machineId);
    }

    private void generateTexture(String machineId) {
        try {
            LOGGER.info("Generating texture for machine " + machineId);

            // Load the base and overlay images
            BufferedImage baseImage = ImageIO.read(new File(BLOCK_TEXTURE_PATH + "/biotech_machine_casing.png"));
            BufferedImage overlayOffImage = ImageIO.read(new File(BLOCK_TEXTURE_PATH + "/" + machineId + "_off_overlay.png"));
            BufferedImage overlayOnImage = ImageIO.read(new File(BLOCK_TEXTURE_PATH + "/" + machineId + "_on_overlay.png"));

            // Create the combined images
            BufferedImage combinedOffImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            BufferedImage combinedOnImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

            // Draw the base and overlay images
            Graphics gOff = combinedOffImage.getGraphics();
            gOff.drawImage(baseImage, 0, 0, null);
            gOff.drawImage(overlayOffImage, 0, 0, null);
            gOff.dispose();

            Graphics gOn = combinedOnImage.getGraphics();
            gOn.drawImage(baseImage, 0, 0, null);
            gOn.drawImage(overlayOnImage, 0, 0, null);
            gOn.dispose();

            // Save the combined images
            File outputDir = new File(GEN_BLOCK_TEXTURE_PATH);
            ImageIO.write(combinedOffImage, "png", new File(outputDir, machineId + "_off.png"));
            ImageIO.write(combinedOnImage, "png", new File(outputDir, machineId + "_on.png"));
        } catch (IOException e) {
            LOGGER.severe("Failed to generate texture for machine " + machineId);
        }
    }

    private void generateModels(String machineId) {
        try {
            // Create the "on" model JSON
            Map<String, Object> onModel = Map.of(
                    "parent", "minecraft:block/orientable",
                    "textures", Map.of(
                            "front", "biotech:block/" + machineId + "_on",
                            "side", "biotech:block/biotech_machine_casing",
                            "top", "biotech:block/biotech_machine_casing"
                    )
            );

            // Create the "off" model JSON
            Map<String, Object> offModel = Map.of(
                    "parent", "minecraft:block/orientable",
                    "textures", Map.of(
                            "front", "biotech:block/" + machineId + "_off",
                            "side", "biotech:block/biotech_machine_casing",
                            "top", "biotech:block/biotech_machine_casing"
                    )
            );

            // Create the block item model JSON
            Map<String, Object> itemModel = Map.of(
                    "parent", "biotech:block/" + machineId + "_on"
            );

            // Write the JSON files to the appropriate paths
            File onModelFile = new File(GEN_BLOCK_MODELS_PATH, machineId + "_on.json");
            File offModelFile = new File(GEN_BLOCK_MODELS_PATH, machineId + "_off.json");
            File itemModelFile = new File(GEN_ITEM_MODELS_PATH, machineId + ".json");

            // Write the JSON content to the files, overwriting if they exist
            try (FileWriter onWriter = new FileWriter(onModelFile);
                 FileWriter offWriter = new FileWriter(offModelFile);
                 FileWriter itemWriter = new FileWriter(itemModelFile)) {
                GSON.toJson(onModel, onWriter);
                GSON.toJson(offModel, offWriter);
                GSON.toJson(itemModel, itemWriter);
            }

            LOGGER.info("Generated models for machine " + machineId);
        } catch (IOException e) {
            LOGGER.severe("Failed to generate models for machine " + machineId);
        }
    }

    private void generateBlockstate(String machineId) {
        try {
            // Create the blockstate JSON
            Map<String, Object> blockstate = Map.of(
                    "variants", Map.of(
                            "facing=north,operating=false", Map.of("model", "biotech:block/" + machineId + "_off"),
                            "facing=north,operating=true", Map.of("model", "biotech:block/" + machineId + "_on"),
                            "facing=east,operating=false", Map.of("model", "biotech:block/" + machineId + "_off", "y", 90),
                            "facing=east,operating=true", Map.of("model", "biotech:block/" + machineId + "_on", "y", 90),
                            "facing=south,operating=false", Map.of("model", "biotech:block/" + machineId + "_off", "y", 180),
                            "facing=south,operating=true", Map.of("model", "biotech:block/" + machineId + "_on", "y", 180),
                            "facing=west,operating=false", Map.of("model", "biotech:block/" + machineId + "_off", "y", 270),
                            "facing=west,operating=true", Map.of("model", "biotech:block/" + machineId + "_on", "y", 270)
                    )
            );

            // Write the JSON file to the appropriate path
            File blockstateFile = new File(GEN_BLOCKSTATES_PATH, machineId + ".json");

            // Write the JSON content to the file, overwriting if it exists
            try (FileWriter writer = new FileWriter(blockstateFile)) {
                GSON.toJson(blockstate, writer);
            }

            LOGGER.info("Generated blockstate for machine " + machineId);
        } catch (IOException e) {
            LOGGER.severe("Failed to generate blockstate for machine " + machineId);
        }
    }

    private void generateLootTable(String machineId) {
        try {
            // Create the loot table JSON structure
            Map<String, Object> lootTable = Map.of(
                    "type", "minecraft:block",
                    "pools", List.of(
                            Map.of(
                                    "bonus_rolls", 0.0,
                                    "conditions", List.of(
                                            Map.of("condition", "minecraft:survives_explosion")
                                    ),
                                    "entries", List.of(
                                            Map.of(
                                                    "type", "minecraft:item",
                                                    "name", "biotech:" + machineId
                                            )
                                    ),
                                    "rolls", 1.0
                            )
                    ),
                    "random_sequence", "biotech:blocks/" + machineId
            );

            // Define the file path
            File lootTableFile = new File(GEN_BLOCK_LOOT_TABLES_PATH, machineId + ".json");

            // Ensure the directory exists
            boolean exist = lootTableFile.getParentFile().mkdirs();

            // Write the JSON content to the file
            try (FileWriter writer = new FileWriter(lootTableFile)) {
                GSON.toJson(lootTable, writer);
            }

            LOGGER.info("Generated loot table for machine " + machineId);
        } catch (IOException e) {
            LOGGER.severe("Failed to generate loot table for machine " + machineId);
        }
    }
}