package com.nstut.biotech.machines;

/** Stable saved/menu IDs. Missing or unknown IDs retain the pre-2.5 behavior. */
public enum RedstoneMode {
    IGNORE, HIGH, LOW;

    public boolean permits(boolean powered) {
        return this == IGNORE || (this == HIGH ? powered : !powered);
    }

    public RedstoneMode next() { return values()[(ordinal() + 1) % values().length]; }
    public static RedstoneMode fromId(int id) {
        return id >= 0 && id < values().length ? values()[id] : IGNORE;
    }
    public String translationKey() { return "ui.biotech.redstone." + name().toLowerCase(java.util.Locale.ROOT); }
}
