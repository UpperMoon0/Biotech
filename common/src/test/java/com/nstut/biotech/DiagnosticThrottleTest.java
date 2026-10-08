package com.nstut.biotech;

import com.nstut.biotech.machines.DiagnosticThrottle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiagnosticThrottleTest {
    @Test void firstDiagnosisIsImmediateAndIdleScansAreBounded() {
        assertTrue(DiagnosticThrottle.shouldRefresh(0, Long.MIN_VALUE));
        for (long tick = 0; tick < 20; tick++) {
            assertFalse(DiagnosticThrottle.shouldRefresh(tick, 0), "duplicate scan at tick " + tick);
        }
        assertTrue(DiagnosticThrottle.shouldRefresh(20, 0));
        assertFalse(DiagnosticThrottle.shouldRefresh(21, 20));
        assertTrue(DiagnosticThrottle.shouldRefresh(5, 100), "time reset must invalidate cache");
    }
}
