package com.nstut.biotech.jei;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Registry-free presentation of the recipe's serialized float probability. */
public final class JeiChancePresentation {
    private JeiChancePresentation() { }

    public static boolean needsLabel(float chance) {
        return chance < 1.0f;
    }

    /** No rounding: Float.toString preserves the recipe float's round-trip decimal value. */
    public static String exactPercent(float chance) {
        return percent(chance).stripTrailingZeros().toPlainString() + "%";
    }

    /** Small enough for a slot; approximations are explicit and never claim 0% or 100%. */
    public static String compactPercent(float chance) {
        BigDecimal exact = percent(chance);
        BigDecimal rounded = exact.setScale(1, RoundingMode.HALF_UP);
        if (chance > 0 && rounded.signum() == 0) return "<0.1%";
        if (chance < 1 && rounded.compareTo(BigDecimal.valueOf(100)) >= 0) return ">99.9%";
        return (rounded.compareTo(exact) == 0 ? "" : "~")
                + rounded.stripTrailingZeros().toPlainString() + "%";
    }

    private static BigDecimal percent(float chance) {
        if (!Float.isFinite(chance) || chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Chance must be finite and between zero and one");
        }
        return new BigDecimal(Float.toString(chance)).movePointRight(2);
    }
}
