package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidContainerInteractionTest {
    @Test
    void fluidHatchesUseGenericNeoForgeFluidContainers() throws IOException {
        Path root = findRepositoryRoot();
        String block = Files.readString(root.resolve("neoforge-1.21.1/src/main/java/com/nstut/biotech/blocks/IOHatchBlock.java"));
        String hatch = Files.readString(root.resolve("neoforge-1.21.1/src/main/java/com/nstut/biotech/blocks/entites/hatches/FluidHatchBlockEntity.java"));
        String menu = Files.readString(root.resolve("neoforge-1.21.1/src/main/java/com/nstut/biotech/views/io_hatches/fluid/FluidHatchMenu.java"));

        assertTrue(block.contains("FluidUtil.interactWithFluidHandler"));
        assertTrue(block.contains("ItemInteractionResult"));
        assertTrue(hatch.contains("Capabilities.FluidHandler.ITEM"));
        assertTrue(hatch.contains("FluidUtil.tryEmptyContainer"));
        assertTrue(hatch.contains("FluidUtil.tryFillContainer"));
        assertTrue(menu.contains("FluidHatchBlockEntity.isFluidContainer"));
        assertFalse(hatch.contains("Items.WATER_BUCKET"));
        assertFalse(hatch.contains("Fluids.WATER"));
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
