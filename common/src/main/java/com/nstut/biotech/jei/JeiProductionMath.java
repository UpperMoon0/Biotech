package com.nstut.biotech.jei;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Nominal, continuously powered production at 20 TPS, including partial final energy ticks. */
public final class JeiProductionMath {
    private JeiProductionMath() { }

    public static long cycleTicks(int energy, int energyPerTick) {
        if (energyPerTick <= 0) throw new IllegalArgumentException("Throughput must be positive");
        return Math.max(1L, (Math.max(0L, energy) + energyPerTick - 1L) / energyPerTick);
    }

    public static String seconds(int energy, int energyPerTick) {
        return BigDecimal.valueOf(cycleTicks(energy, energyPerTick))
                .divide(BigDecimal.valueOf(20)).stripTrailingZeros().toPlainString();
    }

    public static String expectedPerMinute(int count, float chance, int energy, int energyPerTick) {
        if (count < 0 || !Float.isFinite(chance) || chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Invalid output");
        }
        return BigDecimal.valueOf(count).multiply(new BigDecimal(Float.toString(chance)))
                .multiply(BigDecimal.valueOf(1200))
                .divide(BigDecimal.valueOf(cycleTicks(energy, energyPerTick)), 4, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }
}
