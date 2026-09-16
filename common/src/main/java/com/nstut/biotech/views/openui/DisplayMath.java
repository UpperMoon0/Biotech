package com.nstut.biotech.views.openui;

/** Defensive scaling for unsynchronized, empty, and large-capacity gauges. */
public final class DisplayMath {
    private DisplayMath() { }
    public static int fill(int amount, int capacity, int pixels) {
        if (amount <= 0 || capacity <= 0 || pixels <= 0) return 0;
        return (int) Math.min(pixels, Math.max(1L, (long) amount * pixels / capacity));
    }
}
