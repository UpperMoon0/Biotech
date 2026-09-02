package com.nstut.biotech.blocks.entites.hatches;

import net.minecraftforge.energy.EnergyStorage;

import java.util.Objects;

final class DirtyEnergyStorage extends EnergyStorage {
    private final Runnable onChanged;

    DirtyEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        super(capacity, maxReceive, maxExtract);
        this.onChanged = Objects.requireNonNull(onChanged, "onChanged");
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0) {
            onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) {
            onChanged.run();
        }
        return extracted;
    }
}
