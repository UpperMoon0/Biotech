package com.nstut.biotech.recipes;

/** Registry-independent capacitated matching used by prepared animal transactions on every target. */
final class AnimalInputAllocation {
    private AnimalInputAllocation() {}

    static boolean allocateUnit(int animal, boolean[][] matches, int[][] allocated, int[] available,
                                        boolean[] visitedAnimals, boolean[] visitedSlots) {
        if (visitedAnimals[animal]) return false;
        visitedAnimals[animal] = true;
        // Prefer a free matching unit before relocating earlier donors. This keeps first-parent
        // inheritance unchanged whenever the authored greedy ordering already has a valid solution.
        for (int slot = 0; slot < available.length; slot++) {
            if (matches[animal][slot] && !visitedSlots[slot] && available[slot] > 0) {
                allocated[animal][slot]++;
                available[slot]--;
                return true;
            }
        }
        for (int slot = 0; slot < available.length; slot++) {
            if (!matches[animal][slot] || visitedSlots[slot]) continue;
            visitedSlots[slot] = true;
            for (int previous = 0; previous < allocated.length; previous++) {
                if (previous == animal || allocated[previous][slot] == 0) continue;
                if (allocateUnit(previous, matches, allocated, available, visitedAnimals, visitedSlots)) {
                    allocated[previous][slot]--;
                    allocated[animal][slot]++;
                    return true;
                }
            }
        }
        return false;
    }

}
