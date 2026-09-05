package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidMenuInitialSyncTest {
    @Test
    void menuSeedsItsDisplayFromTheSyncedBlockEntityTank() throws IOException {
        Path root = findRepositoryRoot();
        String menu = Files.readString(root.resolve("neoforge-26.1.2/src/main/java/com/nstut/biotech/views/io_hatches/fluid/FluidHatchMenu.java"));
        assertTrue(menu.contains("BLOCK_ENTITY.getInternalTank().getFluidInTank(0).copy()"));
        assertFalse(menu.contains("fluidStack = FluidStack.EMPTY"));
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
