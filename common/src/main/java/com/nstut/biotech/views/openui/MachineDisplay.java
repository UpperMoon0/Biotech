package com.nstut.biotech.views.openui;

import com.nstut.nstutlib.recipes.ModRecipeData;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Live, read-only bindings to the existing synchronized menu. */
public record MachineDisplay(BooleanSupplier valid, BooleanSupplier operating,
        IntSupplier stored, IntSupplier capacity, IntSupplier consumed,
        IntSupplier cost, IntSupplier rate, Supplier<ModRecipeData> recipe) {
    public boolean active() { return valid.getAsBoolean() && operating.getAsBoolean() && recipe.get() != null; }
    public String status() { return !valid.getAsBoolean() ? "Invalid Structure" : active() ? "Operating" : "Not Operating"; }
    public String energyTooltip() {
        if (!valid.getAsBoolean()) return status();
        return "Stored Energy:\n" + stored.getAsInt() + " / " + capacity.getAsInt() + " FE"
                + (active() ? "\nRecipe Energy:\n" + consumed.getAsInt() + " / " + cost.getAsInt() + " FE" : "")
                + "\nRate: " + (active() ? rate.getAsInt() : 0) + " FE / t";
    }
    public String progressTooltip() {
        if (!active()) return status();
        return "Progress:\n" + consumed.getAsInt() + " / " + cost.getAsInt() + " FE\n"
                + String.format(java.util.Locale.ROOT, "%.1f / %.1f s",
                    com.nstut.biotech.views.machines.screen.MachineScreenMath.secondsForEnergy(consumed.getAsInt(), rate.getAsInt()),
                    com.nstut.biotech.views.machines.screen.MachineScreenMath.secondsForEnergy(cost.getAsInt(), rate.getAsInt()));
    }
}
