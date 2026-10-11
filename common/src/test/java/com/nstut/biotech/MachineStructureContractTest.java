package com.nstut.biotech;

import com.google.gson.JsonParser;
import com.nstut.biotech.machines.MachineStructures;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class MachineStructureContractTest {
    @Test void everyNativeLayoutHasOneControllerAndUniquePortOffsets() {
        for (String id : MachineStructures.ids()) {
            String[][] layers = MachineStructures.layers(id);
            int[] origin = MachineStructures.origin(id);
            assertEquals('@', layers[layers.length-origin[1]-1][layers[0].length-origin[2]-1].charAt(origin[0]), id);
            int controllers = 0;
            var symbols = new HashSet<Character>();
            for (String[] layer : layers) {
                assertEquals(layers[0].length, layer.length, id);
                for (String row : layer) {
                    assertEquals(layers[0][0].length(), row.length(), id);
                    for (char symbol : row.toCharArray()) {
                        if (symbol == '@') controllers++;
                        if (Character.isDigit(symbol)) assertTrue(symbols.add(symbol), id + " duplicate port");
                    }
                }
            }
            assertEquals(1, controllers, id);
            assertEquals(symbols.size(), MachineStructures.hatchOffsets(id).length, id);
            var offsets = new HashSet<>();
            for (var offset : MachineStructures.hatchOffsets(id)) assertTrue(offsets.add(offset), id);
        }
    }

    @Test void habitatEntranceAndInteriorHaveThreeBlocksOfRequiredClearance() {
        String[][] layers = MachineStructures.layers("terrestrial_habitat");
        assertEquals(5, layers.length);
        for (int y = 1; y <= 3; y++) for (int z = 1; z <= 6; z++) for (int x = 4; x <= 5; x++)
            assertEquals('.', layers[y][z].charAt(x), "clear doorway-to-shelter route");
        assertEquals("FPPPPPF", layers[0][6], "supported entrance lintel");
        assertEquals("FPP@..F", layers[3][6], "controller beside doorway");
        assertEquals(4, MachineStructures.habitat26Layers().length, "2.6 remains frozen");
    }

    @Test void guidePreviewExactlyMatchesNativeLayers() throws Exception {
        Path root = Path.of("").toAbsolutePath();
        while (!Files.exists(root.resolve("design/machines.json"))) root = root.getParent();
        for (String id : MachineStructures.ids()) {
            var entry = JsonParser.parseString(Files.readString(root.resolve(
                    "common/src/main/resources/assets/biotech/patchouli_books/biotech_guide_book/en_us/entries/"+id+".json"))).getAsJsonObject();
            var pages = entry.getAsJsonArray("pages");
            int previews = 0;
            for (var element : pages) {
                var page = element.getAsJsonObject();
                if (!page.has("multiblock")) continue;
                previews++;
                var preview = page.getAsJsonObject("multiblock");
                assertFalse(preview.get("symmetrical").getAsBoolean(), id);
                assertEquals("biotech:"+id+"[facing=south]", preview.getAsJsonObject("mapping").get("0").getAsString());
                String[][] nativeLayers = MachineStructures.layers(id);
                var guideLayers = preview.getAsJsonArray("pattern");
                assertEquals(nativeLayers.length, guideLayers.size(), id);
                for (int y=0; y<nativeLayers.length; y++) {
                    var rows = guideLayers.get(y).getAsJsonArray();
                    assertEquals(nativeLayers[y].length, rows.size(), id);
                    for (int z=0; z<nativeLayers[y].length; z++) assertEquals(nativeLayers[y][z].replace('@','0'), rows.get(z).getAsString(), id);
                }
            }
            assertEquals(1, previews, id);
        }
    }
}
