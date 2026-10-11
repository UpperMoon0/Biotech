package com.nstut.biotech.data;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MachineTextureTest {
    private static final List<String> TARGETS = List.of("forge-1.20.1", "neoforge-1.21.1", "neoforge-26.1.2");

    @Test void largerOverlayIsNotClippedAndCasingUsesNearestNeighbor() {
        BufferedImage base = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        base.setRGB(0, 0, 0xff123456);
        base.setRGB(1, 0, 0xffabcdef);
        BufferedImage overlay = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        overlay.setRGB(31, 31, 0xff75da42);
        BufferedImage result = MachineGenerator.combineTexture(base, overlay);
        assertEquals(32, result.getWidth());
        assertEquals(32, result.getHeight());
        assertEquals(0xff123456, result.getRGB(0, 0));
        assertEquals(0xff123456, result.getRGB(1, 1));
        assertEquals(0xffabcdef, result.getRGB(2, 0));
        assertEquals(0xff75da42, result.getRGB(31, 31));
        assertEquals(0, result.getRGB(10, 10), "unpainted pixels remain transparent");
    }

    @Test void smallerOverlayAlsoScalesWithHardPixelEdges() {
        BufferedImage base = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        BufferedImage overlay = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        overlay.setRGB(15, 15, 0xff75da42);
        BufferedImage result = MachineGenerator.combineTexture(base, overlay);
        assertEquals(0xff75da42, result.getRGB(30, 30));
        assertEquals(0xff75da42, result.getRGB(31, 31));
        assertEquals(0, result.getRGB(29, 29));
    }

    @Test void existingControllersKeepTheirOriginalCompositedPixels() throws Exception {
        Path textures = root().resolve("common/src/main/resources/assets/biotech/textures/block");
        BufferedImage base = ImageIO.read(textures.resolve("biotech_machine_casing.png").toFile());
        for (String machine : List.of("breeding_chamber", "slaughterhouse", "greenhouse", "fermenter", "mixer")) {
            for (String state : List.of("off", "on")) {
                BufferedImage overlay = ImageIO.read(textures.resolve(machine + "_" + state + "_overlay.png").toFile());
                BufferedImage legacy = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = legacy.createGraphics();
                try {
                    graphics.drawImage(base, 0, 0, null);
                    graphics.drawImage(overlay, 0, 0, null);
                } finally {
                    graphics.dispose();
                }
                assertPixelsEqual(legacy, MachineGenerator.combineTexture(base, overlay), machine + " " + state);
            }
        }
    }

    @Test void habitatFacesAre32pxAndMatchEveryNativeTarget() throws Exception {
        Path root = root();
        Path textures = root.resolve("common/src/main/resources/assets/biotech/textures/block");
        BufferedImage base = ImageIO.read(textures.resolve("biotech_machine_casing.png").toFile());
        BufferedImage off = null;
        BufferedImage on = null;
        for (String state : List.of("off", "on")) {
            BufferedImage overlay = ImageIO.read(textures.resolve("terrestrial_habitat_" + state + "_overlay.png").toFile());
            assertEquals(32, overlay.getWidth());
            assertEquals(32, overlay.getHeight());
            BufferedImage expected = MachineGenerator.combineTexture(base, overlay);
            if (state.equals("off")) off = expected; else on = expected;
            for (String target : TARGETS) {
                Path generated = root.resolve(target + "/src/generated/resources/assets/biotech/textures/block/terrestrial_habitat_" + state + ".png");
                assertPixelsEqual(expected, ImageIO.read(generated.toFile()), target + " " + state);
            }
        }
        assertNotNull(off);
        assertNotNull(on);
        assertNotEquals(off.getRGB(16, 14), on.getRGB(16, 14), "growth indicator must distinguish active state");
    }

    private static void assertPixelsEqual(BufferedImage expected, BufferedImage actual, String context) {
        assertEquals(expected.getWidth(), actual.getWidth(), context);
        assertEquals(expected.getHeight(), actual.getHeight(), context);
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                assertEquals(expected.getRGB(x, y), actual.getRGB(x, y), context + " pixel " + x + "," + y);
            }
        }
    }

    private static Path root() {
        Path root = Path.of("").toAbsolutePath();
        while (root != null && !Files.exists(root.resolve("design/machines.json"))) root = root.getParent();
        assertNotNull(root, "repository root");
        return root;
    }
}
