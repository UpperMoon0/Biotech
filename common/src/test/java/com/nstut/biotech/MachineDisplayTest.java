package com.nstut.biotech;

import com.nstut.biotech.views.openui.MachineDisplay;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MachineDisplayTest {
    @Test void delayedRecipeSyncIsSafeAndBindingsKeepUpdating() {
        var valid = new AtomicBoolean(false);
        var energy = new AtomicInteger(100);
        var display = new MachineDisplay(valid::get, () -> true, energy::get,
                () -> 1000, () -> 25, () -> 100, () -> 64, () -> null);
        assertEquals("Invalid Structure", display.status());
        assertEquals("Invalid Structure", display.energyTooltip());
        valid.set(true);
        assertFalse(display.active());
        assertEquals("Not Operating", display.progressTooltip());
        assertTrue(display.energyTooltip().contains("100 / 1000 FE"));
        assertTrue(display.energyTooltip().contains("Rate: 0 FE / t"));
        energy.set(500);
        assertTrue(display.energyTooltip().contains("500 / 1000 FE"));
        valid.set(false);
        assertEquals("Invalid Structure", display.energyTooltip());
    }
}
