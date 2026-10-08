package com.nstut.biotech.machines;

/** Limit informational idle recipe scans to once per second per controller. */
public final class DiagnosticThrottle {
    private DiagnosticThrottle() {}

    public static boolean shouldRefresh(long gameTick, long lastTick) {
        return lastTick == Long.MIN_VALUE || gameTick < lastTick || gameTick - lastTick >= 20;
    }
}
