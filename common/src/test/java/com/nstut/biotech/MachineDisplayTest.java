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
        assertEquals("ui.biotech.status.invalid_structure", display.status());
        assertEquals("ui.biotech.status.invalid_structure", display.energyTooltip());
        valid.set(true);
        assertFalse(display.active());
        assertEquals("ui.biotech.status.no_matching_recipe", display.progressTooltip());
        assertEquals(100, display.stored().getAsInt());
        assertEquals(com.nstut.biotech.machines.MachineStatus.NO_MATCHING_RECIPE, display.diagnostic().get());
        energy.set(500);
        assertEquals(500, display.stored().getAsInt());
        valid.set(false);
        assertEquals("ui.biotech.status.invalid_structure", display.energyTooltip());
    }
}
