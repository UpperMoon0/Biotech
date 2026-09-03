package com.nstut.biotech.blocks.entites.hatches;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

import java.util.Objects;

final class DirtyEnergyStorage extends SimpleEnergyHandler {
    private final Runnable onChanged;

    DirtyEnergyStorage(int capacity, int maxInsert, int maxExtract, Runnable onChanged) {
        super(capacity, maxInsert, maxExtract);
        this.onChanged = Objects.requireNonNull(onChanged, "onChanged");
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        onChanged.run();
    }

    void setAbsoluteEnergy(int energy) {
        set(Mth.clamp(energy, 0, getCapacityAsInt()));
    }
}
