package com.nstut.biotech.jei;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JeiProductionMathTest {
    @Test void cycleTimeRoundsUpPartialEnergyTicks() {
        assertEquals(63, JeiProductionMath.cycleTicks(32000, 512));
        assertEquals("3.15", JeiProductionMath.seconds(32000, 512));
        assertEquals(125, JeiProductionMath.cycleTicks(64000, 512));
        assertEquals("6.25", JeiProductionMath.seconds(64000, 512));
        assertEquals(1, JeiProductionMath.cycleTicks(0, 512));
        assertEquals(1, JeiProductionMath.cycleTicks(1, 512));
        assertEquals(1, JeiProductionMath.cycleTicks(512, 512));
        assertEquals(2, JeiProductionMath.cycleTicks(513, 512));
        assertEquals(4194304L, JeiProductionMath.cycleTicks(Integer.MAX_VALUE, 512));
    }

    @Test void ratesAccountForStackSizeProbabilityAndFluidAmount() {
        assertEquals("19.0476", JeiProductionMath.expectedPerMinute(1, 1, 32000, 512));
        assertEquals("28.8", JeiProductionMath.expectedPerMinute(3, 1, 64000, 512));
        assertEquals("9.5238", JeiProductionMath.expectedPerMinute(1, 0.5f, 32000, 512));
        assertEquals("19047.619", JeiProductionMath.expectedPerMinute(1000, 1, 32000, 512));
        assertEquals("0", JeiProductionMath.expectedPerMinute(1, 0, 32000, 512));
        assertThrows(IllegalArgumentException.class, () -> JeiProductionMath.cycleTicks(1, 0));
    }
}
