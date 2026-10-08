package com.nstut.biotech;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlaughterhouseYieldConfigTest {
    @Test
    void unloadingResetsDefaultWithoutReadingTheAlreadyClosedConfig() {
        int reset = SlaughterhouseYieldConfig.forEvent(true, () -> {
            throw new IllegalStateException("Cannot get config value before config is loaded");
        });
        assertEquals(2, reset);
    }

    @Test
    void loadingAndReloadingReadTheLatestServerSetting() {
        assertEquals(7, SlaughterhouseYieldConfig.forEvent(false, () -> 7));
        assertEquals(64, SlaughterhouseYieldConfig.forEvent(false, () -> 64));
        assertEquals(1, SlaughterhouseYieldConfig.forEvent(false, () -> 1));
    }
}
