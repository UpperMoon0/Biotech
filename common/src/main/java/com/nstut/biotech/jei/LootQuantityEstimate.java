package com.nstut.biotech.jei;

import java.util.Locale;

/** Shared sampling policy and compact approximate item quantities. */
public final class LootQuantityEstimate {
    public static final int SAMPLES = 256;
    private LootQuantityEstimate() {}
    public static long seed(int sample) {
        // SplitMix64: independent nonzero seeds, never the world's shared loot sequence.
        long value = 0x9E3779B97F4A7C15L * (sample + 1L);
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return value == 0 ? 1 : value;
    }
    public static String label(double mean, int multiplier) {
        double quantity = mean * multiplier;
        if (!(quantity > 0) || !Double.isFinite(quantity)) return "?";
        if (quantity < 0.1) return "~<0.1";
        if (quantity < 1) return "~" + String.format(Locale.ROOT, "%.1f", quantity);
        return "~" + Math.round(quantity);
    }
    public static String precise(double mean, int multiplier) {
        return String.format(Locale.ROOT, "%.2f", mean * multiplier);
    }
}
