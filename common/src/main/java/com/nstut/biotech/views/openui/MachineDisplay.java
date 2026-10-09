package com.nstut.biotech.views.openui;

import com.nstut.nstutlib.recipes.ModRecipeData;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Live, read-only bindings to the existing synchronized menu. */
public record MachineDisplay(BooleanSupplier valid, BooleanSupplier operating,
        IntSupplier stored, IntSupplier capacity, IntSupplier consumed,
        IntSupplier cost, IntSupplier rate, Supplier<ModRecipeData> recipe,
        Supplier<com.nstut.biotech.machines.MachineStatus> diagnostic,
        Supplier<com.nstut.biotech.machines.RedstoneMode> mode, Runnable cycleMode) {
    public MachineDisplay(BooleanSupplier valid, BooleanSupplier operating, IntSupplier stored,
            IntSupplier capacity, IntSupplier consumed, IntSupplier cost, IntSupplier rate, Supplier<ModRecipeData> recipe) {
        this(valid, operating, stored, capacity, consumed, cost, rate, recipe,
                () -> !valid.getAsBoolean() ? com.nstut.biotech.machines.MachineStatus.INVALID_STRUCTURE
                    : operating.getAsBoolean() && recipe.get() != null ? com.nstut.biotech.machines.MachineStatus.PROCESSING
                    : com.nstut.biotech.machines.MachineStatus.NO_MATCHING_RECIPE,
                () -> com.nstut.biotech.machines.RedstoneMode.IGNORE, () -> {});
    }
    public boolean active() { return valid.getAsBoolean() && operating.getAsBoolean() && recipe.get() != null; }
    public String status() { return net.minecraft.network.chat.Component.translatable(diagnostic.get().translationKey()).getString(); }
    public String energyTooltip() {
        if (!valid.getAsBoolean()) return status();
        return net.minecraft.network.chat.Component.translatable("ui.biotech.energy.stored", stored.getAsInt(), capacity.getAsInt()).getString()
                + (active() ? "\n" + net.minecraft.network.chat.Component.translatable("ui.biotech.energy.recipe", consumed.getAsInt(), cost.getAsInt()).getString() : "")
                + "\n" + net.minecraft.network.chat.Component.translatable("ui.biotech.energy.rate", diagnostic.get() == com.nstut.biotech.machines.MachineStatus.PROCESSING ? rate.getAsInt() : 0).getString();
    }
    public String progressTooltip() {
        if (!active()) return status();
        return status() + "\n" + net.minecraft.network.chat.Component.translatable("ui.biotech.energy.progress", consumed.getAsInt(), cost.getAsInt()).getString() + "\n"
                + String.format(java.util.Locale.ROOT, "%.1f / %.1f s",
                    com.nstut.biotech.views.machines.screen.MachineScreenMath.secondsForEnergy(consumed.getAsInt(), rate.getAsInt()),
                    com.nstut.biotech.views.machines.screen.MachineScreenMath.secondsForEnergy(cost.getAsInt(), rate.getAsInt()));
    }
}
