package com.nstut.biotech.blocks.entites.hatches;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FluidHatchCapabilityContractTest {
    @Test
    void outputHatchAdvertisesNoInsertionCapacity() {
        assertEquals(32_000L, FluidHatchBlockEntity.externalInsertionCapacity(true, 32_000L));
        assertEquals(0L, FluidHatchBlockEntity.externalInsertionCapacity(false, 32_000L));
    }
}
