package com.nstut.biotech.blocks.entites.hatches;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirtyEnergyStorageTest {
    @Test
    void marksDirtyOnlyForCommittedMutations() {
        AtomicInteger changes = new AtomicInteger();
        DirtyEnergyStorage storage = new DirtyEnergyStorage(1000, 100, 100, changes::incrementAndGet);

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(100, storage.insert(100, transaction));
        }
        assertEquals(0, changes.get());
        assertEquals(0, storage.getAmountAsInt());

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(100, storage.insert(100, transaction));
            transaction.commit();
        }
        assertEquals(1, changes.get());
        assertEquals(100, storage.getAmountAsInt());

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(100, storage.extract(100, transaction));
        }
        assertEquals(1, changes.get());
        assertEquals(100, storage.getAmountAsInt());

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(100, storage.extract(100, transaction));
            transaction.commit();
        }
        assertEquals(2, changes.get());
        assertEquals(0, storage.getAmountAsInt());

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(0, storage.extract(100, transaction));
            transaction.commit();
        }
        assertEquals(2, changes.get());
    }
}
