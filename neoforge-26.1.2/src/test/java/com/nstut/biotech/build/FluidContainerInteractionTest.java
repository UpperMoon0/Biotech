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
        Path repositoryRoot = findRepositoryRoot();
        String block = Files.readString(repositoryRoot.resolve(Path.of(
                "neoforge-26.1.2", "src", "main", "java", "com", "nstut", "biotech", "blocks", "IOHatchBlock.java")));
        String hatch = Files.readString(repositoryRoot.resolve(Path.of(
                "neoforge-26.1.2", "src", "main", "java", "com", "nstut", "biotech", "blocks", "entites", "hatches", "FluidHatchBlockEntity.java")));
        String menu = Files.readString(repositoryRoot.resolve(Path.of(
                "neoforge-26.1.2", "src", "main", "java", "com", "nstut", "biotech", "views", "io_hatches", "fluid", "FluidHatchMenu.java")));

        assertTrue(block.contains("FluidUtil.interactWithFluidHandler"), "right-click interaction must use NeoForge's generic fluid-container helper");
        assertTrue(block.contains("getManualFluidStorage"), "manual transfer must use the hatch's directional fluid view");
        assertTrue(hatch.contains("Capabilities.Fluid.ITEM"), "container detection must use the generic item fluid capability");
        assertTrue(hatch.contains("ItemAccess.forHandlerIndexStrict"), "slot processing must isolate one container so item replacement is safe");
        assertTrue(hatch.contains("ResourceHandlerUtil.moveFirst"), "slot processing must use capability-driven fluid transfer");
        assertTrue(menu.contains("FluidHatchBlockEntity.isFluidContainer"), "input slot must accept arbitrary fluid-capable containers");
        assertFalse(hatch.contains("Items.WATER_BUCKET"), "fluid hatch processing must not be hardcoded to vanilla water buckets");
        assertFalse(hatch.contains("Fluids.WATER"), "fluid hatch processing must not be hardcoded to water");
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
