package com.nstut.biotech.machines;

/** Server-authoritative diagnostics, synchronized through the vanilla menu. */
public enum MachineStatus {
    INVALID_STRUCTURE, NO_MATCHING_RECIPE, MISSING_ITEMS, MISSING_FLUID,
    INSUFFICIENT_ENERGY, ITEM_OUTPUT_BLOCKED, FLUID_OUTPUT_BLOCKED, PROCESSING,
    REDSTONE_PAUSED, TRANSACTION_ERROR;

    public static MachineStatus fromId(int id) {
        return id >= 0 && id < values().length ? values()[id] : INVALID_STRUCTURE;
    }
    public String translationKey() { return "ui.biotech.status." + name().toLowerCase(java.util.Locale.ROOT); }
}
