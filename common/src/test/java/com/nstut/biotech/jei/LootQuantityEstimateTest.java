package com.nstut.biotech.jei;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class LootQuantityEstimateTest {
    @Test void quantitiesIncludeYieldAndPreserveRareUnknownAndLargeOutputs() {
        assertEquals("~4", LootQuantityEstimate.label(2.04, 2));
        assertEquals("~0.5", LootQuantityEstimate.label(0.25, 2));
        assertEquals("~<0.1", LootQuantityEstimate.label(0.003, 2));
        assertEquals("?", LootQuantityEstimate.label(0, 2));
        assertEquals("~128", LootQuantityEstimate.label(64, 2));
        assertEquals("4.08", LootQuantityEstimate.precise(2.04, 2));
    }
    @Test void samplesNeverUseWorldRandomSequenceOrDuplicateSeeds() {
        var seeds = new HashSet<Long>();
        for (int i = 0; i < LootQuantityEstimate.SAMPLES; i++) {
            long seed = LootQuantityEstimate.seed(i);
            assertNotEquals(0, seed);
            assertTrue(seeds.add(seed));
            assertEquals(seed, LootQuantityEstimate.seed(i));
        }
    }
}
