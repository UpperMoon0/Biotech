package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.Biotech;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder(Biotech.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DirtyEnergyStorageGameTests {
    private DirtyEnergyStorageGameTests() {
    }

    @GameTest(templateNamespace = "forge", template = "empty3x3x3", timeoutTicks = 40)
    public static void dirtyCallbackOnlyRunsForExecutedMutation(GameTestHelper helper) {
        AtomicInteger changes = new AtomicInteger();
        DirtyEnergyStorage storage = new DirtyEnergyStorage(1000, 100, 100, changes::incrementAndGet);

        helper.assertTrue(storage.receiveEnergy(100, true) == 100, "Simulated receive should report capacity");
        helper.assertTrue(storage.getEnergyStored() == 0 && changes.get() == 0,
                "Simulated receive must not mutate or mark dirty");

        helper.assertTrue(storage.receiveEnergy(100, false) == 100, "Executed receive should accept energy");
        helper.assertTrue(storage.getEnergyStored() == 100 && changes.get() == 1,
                "Executed receive must mark dirty exactly once");

        helper.assertTrue(storage.extractEnergy(100, true) == 100 && changes.get() == 1,
                "Simulated extract must not mark dirty");
        helper.assertTrue(storage.extractEnergy(100, false) == 100, "Executed extract should remove energy");
        helper.assertTrue(storage.getEnergyStored() == 0 && changes.get() == 2,
                "Executed extract must mark dirty exactly once");
        helper.assertTrue(storage.extractEnergy(100, false) == 0 && changes.get() == 2,
                "No-op extraction must not mark dirty");

        helper.succeed();
    }
}
