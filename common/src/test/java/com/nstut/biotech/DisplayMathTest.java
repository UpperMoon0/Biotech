package com.nstut.biotech;

import com.nstut.biotech.views.openui.DisplayMath;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DisplayMathTest {
    @Test void emptyAndUnsynchronizedGaugesStayEmpty() {
        assertEquals(0, DisplayMath.fill(0, 32000, 52));
        assertEquals(0, DisplayMath.fill(50, 0, 52));
        assertEquals(0, DisplayMath.fill(-1, 32000, 52));
    }
    @Test void smallAmountsRemainVisibleAndOverfilledGaugesStayInBounds() {
        assertEquals(1, DisplayMath.fill(1, 614400, 76));
        assertEquals(38, DisplayMath.fill(307200, 614400, 76));
        assertEquals(76, DisplayMath.fill(Integer.MAX_VALUE, 614400, 76));
    }
    @Test void largeCapacitiesDoNotOverflow() {
        assertEquals(76, DisplayMath.fill(Integer.MAX_VALUE, Integer.MAX_VALUE, 76));
        assertEquals(37, DisplayMath.fill(Integer.MAX_VALUE / 2, Integer.MAX_VALUE, 76));
    }
}
