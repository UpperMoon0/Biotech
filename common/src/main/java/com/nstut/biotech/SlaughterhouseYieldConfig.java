package com.nstut.biotech;

import java.util.function.IntSupplier;

/** The loader closes its config before broadcasting Unloading; cached values must reset without reads. */
public final class SlaughterhouseYieldConfig {
    public static final int DEFAULT = 2;

    private SlaughterhouseYieldConfig() {}

    public static int forEvent(boolean unloading, IntSupplier loadedValue) {
        return unloading ? DEFAULT : loadedValue.getAsInt();
    }
}
