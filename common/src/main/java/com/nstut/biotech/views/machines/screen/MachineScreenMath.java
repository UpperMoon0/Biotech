package com.nstut.biotech.views.machines.screen;

public final class MachineScreenMath {
    private MachineScreenMath() {
    }

    public static int progressWidth(int energyConsumed, int totalEnergy, int maxWidth) {
        if (totalEnergy <= 0 || maxWidth <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(maxWidth, energyConsumed * maxWidth / totalEnergy));
    }

    public static float secondsForEnergy(int energy, int energyPerTick) {
        return energyPerTick <= 0 ? 0.0F : (float) energy / energyPerTick / 20.0F;
    }
}