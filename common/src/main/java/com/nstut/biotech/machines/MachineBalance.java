package com.nstut.biotech.machines;

public final class MachineBalance {
    private MachineBalance() { }

    /** Saturating arithmetic preserves free recipes and avoids overflow in large datapacks. */
    public static int energyCost(int base, double multiplier) {
        if (base <= 0) return 0;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, Math.ceil(base * multiplier)));
    }
}
