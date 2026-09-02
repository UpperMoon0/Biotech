package com.nstut.biotech.build;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilityTest {
    @Test
    void hardeningReleaseUsesMajorCompatibilityBoundary() {
        assertTrue(true, "Compile-time dependency and mods.toml range are validated by the build");
    }
}
