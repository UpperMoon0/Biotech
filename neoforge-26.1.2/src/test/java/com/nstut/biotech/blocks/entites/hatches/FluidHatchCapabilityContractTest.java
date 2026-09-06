package com.nstut.biotech.blocks.entites.hatches;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FluidHatchCapabilityContractTest {
    @Test
    void generalCapacitySurvivesWhileOutputInsertionCapacityIsZero() {
        assertEquals(32_000L, FluidHatchCapabilityPolicy.externalCapacity(true, false, 32_000L));
        assertEquals(32_000L, FluidHatchCapabilityPolicy.externalCapacity(true, true, 32_000L));
        assertEquals(0L, FluidHatchCapabilityPolicy.externalCapacity(false, false, 32_000L));
        assertEquals(32_000L, FluidHatchCapabilityPolicy.externalCapacity(false, true, 32_000L));
    }
}
