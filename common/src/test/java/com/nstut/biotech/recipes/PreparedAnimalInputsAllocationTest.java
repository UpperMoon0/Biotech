package com.nstut.biotech.recipes;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Independent small exhaustive oracle for the production capacitated animal allocation step. */
class PreparedAnimalInputsAllocationTest {
    @Test
    void allocationMatchesExhaustiveOracleAndPreservesEveryCapacity() throws ReflectiveOperationException {
        Method allocate = PreparedAnimalInputs.class.getDeclaredMethod("allocateUnit", int.class,
                boolean[][].class, int[][].class, int[].class, boolean[].class, boolean[].class);
        allocate.setAccessible(true);
        Random random = new Random(42);
        for (int trial = 0; trial < 20_000; trial++) {
            int animals = 1 + random.nextInt(4);
            int slots = 1 + random.nextInt(4);
            int[] required = new int[animals];
            int[] free = new int[slots];
            boolean[][] matches = new boolean[animals][slots];
            for (int animal = 0; animal < animals; animal++) {
                required[animal] = random.nextInt(3);
                for (int slot = 0; slot < slots; slot++) matches[animal][slot] = random.nextBoolean();
            }
            for (int slot = 0; slot < slots; slot++) free[slot] = random.nextInt(3);
            int[] initial = free.clone();
            boolean expected = exhaustive(required.clone(), matches, free.clone(), 0);
            int[][] allocated = new int[animals][slots];
            boolean actual = true;
            outer:
            for (int animal = 0; animal < animals; animal++) {
                for (int unit = 0; unit < required[animal]; unit++) {
                    if (!(boolean) allocate.invoke(null, animal, matches, allocated, free,
                            new boolean[animals], new boolean[slots])) {
                        actual = false;
                        break outer;
                    }
                }
            }
            assertEquals(expected, actual, "Complete allocation feasibility, trial " + trial);
            for (int slot = 0; slot < slots; slot++) {
                int used = 0;
                for (int animal = 0; animal < animals; animal++) {
                    assertTrue(allocated[animal][slot] >= 0);
                    assertTrue(allocated[animal][slot] == 0 || matches[animal][slot]);
                    used += allocated[animal][slot];
                }
                assertTrue(free[slot] >= 0);
                assertEquals(initial[slot], used + free[slot], "No input duplication or loss");
            }
            if (actual) {
                for (int animal = 0; animal < animals; animal++) {
                    assertEquals(required[animal], Arrays.stream(allocated[animal]).sum(), "Exact ingredient quantity");
                }
            }
        }
    }

    private static boolean exhaustive(int[] required, boolean[][] matches, int[] free, int animal) {
        while (animal < required.length && required[animal] == 0) animal++;
        if (animal == required.length) return true;
        required[animal]--;
        for (int slot = 0; slot < free.length; slot++) {
            if (!matches[animal][slot] || free[slot] == 0) continue;
            free[slot]--;
            boolean complete = exhaustive(required, matches, free, animal);
            free[slot]++;
            if (complete) {
                required[animal]++;
                return true;
            }
        }
        required[animal]++;
        return false;
    }
}
