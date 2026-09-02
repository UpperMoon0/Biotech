package com.nstut.biotech.blocks.entites.hatches;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirtyEnergyStorageTest {
    @Test
    void marksDirtyOnlyForSuccessfulExecutedMutations() {
        AtomicInteger changes = new AtomicInteger();
        DirtyEnergyStorage storage = new DirtyEnergyStorage(1000, 100, 100, changes::incrementAndGet);

        assertEquals(100, storage.receiveEnergy(100, true));
        assertEquals(0, changes.get());
        assertEquals(0, storage.getEnergyStored());

        assertEquals(100, storage.receiveEnergy(100, false));
        assertEquals(1, changes.get());
        assertEquals(100, storage.getEnergyStored());

        assertEquals(100, storage.extractEnergy(100, true));
        assertEquals(1, changes.get());
        assertEquals(100, storage.getEnergyStored());

        assertEquals(100, storage.extractEnergy(100, false));
        assertEquals(2, changes.get());
        assertEquals(0, storage.getEnergyStored());

        assertEquals(0, storage.extractEnergy(100, false));
        assertEquals(2, changes.get());
    }
}
