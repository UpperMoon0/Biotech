package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.Config;
import com.nstut.biotech.machines.*;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.recipes.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import java.util.*;
import java.util.function.UnaryOperator;

/** Control and read-only diagnostics around NsTutLib's authoritative transaction engine. */
public abstract class ControlledMachineBlockEntity extends ControlStorageBlockEntity {
    private MachineStatus status = MachineStatus.NO_MATCHING_RECIPE;
    private ModRecipe<?> observedRecipe;
    private int[] observedRolls;
    private long lastDiagnosisTick = Long.MIN_VALUE;
    private MachineStatus cachedDiagnosis = MachineStatus.NO_MATCHING_RECIPE;

    protected ControlledMachineBlockEntity(BlockEntityType<? extends MachineBlockEntity> type, BlockPos pos,
            BlockState state, int x, int y, int z) { super(type, pos, state, x, y, z); }
    public MachineStatus getMachineStatus() {
        if (!isStructureValid) return MachineStatus.INVALID_STRUCTURE;
        if (level != null && !redstoneMode.permits(level.hasNeighborSignal(worldPosition))) return MachineStatus.REDSTONE_PAUSED;
        return status;
    }
    /** Read-only recipe view remains available before the provider restores a paused cycle. */
    public ModRecipeData getDisplayRecipe() {
        return recipeHandler.map(ModRecipe::getRecipe).orElse(savedDisplayRecipe);
    }
    public RedstoneMode getRedstoneMode() { return redstoneMode; }
    public void setRedstoneMode(RedstoneMode mode) {
        if (level != null && level.isClientSide) return;
        redstoneMode = Objects.requireNonNull(mode);
        setChanged();
    }
    protected <R extends ModRecipe<R>> void processControlledRecipeTransaction(Level level, RecipeType<R> type,
            IItemHandler inputs, List<? extends IFluidHandler> fluids, IItemHandler outputs,
            List<? extends IFluidHandler> outputFluids, IEnergyStorage energy, int rate) {
        processControlledRecipeTransaction(level, type, inputs, fluids, outputs, outputFluids, energy, rate, null, null);
    }
    protected <R extends ModRecipe<R>> void processControlledRecipeTransaction(Level level, RecipeType<R> type,
            IItemHandler inputs, List<? extends IFluidHandler> fluids, IItemHandler outputs,
            List<? extends IFluidHandler> outputFluids, IEnergyStorage energy, int rate, Comparator<R> preference) {
        processControlledRecipeTransaction(level, type, inputs, fluids, outputs, outputFluids, energy, rate, preference, null);
    }
    protected <R extends ModRecipe<R>> void processControlledRecipeTransaction(Level level, RecipeType<R> type,
            IItemHandler inputs, List<? extends IFluidHandler> fluids, IItemHandler outputs,
            List<? extends IFluidHandler> outputFluids, IEnergyStorage energy, int rate,
            Comparator<R> preference, UnaryOperator<R> preparation) {
        if (!redstoneMode.permits(level.hasNeighborSignal(worldPosition))) { status = MachineStatus.REDSTONE_PAUSED; return; }
        int before = energyConsumed;
        try {
            processRecipeTransaction(level, type, inputs, fluids, outputs, outputFluids, energy,
                    Config.machineEnergyPerTick, preference, recipe -> {
                        R prepared = preparation == null ? recipe : preparation.apply(recipe);
                        ModRecipeData data = prepared.getRecipe().copy();
                        return prepared.create(prepared.getId(), new ModRecipeData(data.getIngredientItems(),
                                data.getOutputItems(), data.getFluidIngredients(), data.getFluidOutputs(),
                                MachineBalance.energyCost(data.getTotalEnergy(), Config.machineEnergyMultiplier)));
                    });
        } catch (RecipeTransactionException exception) {
            status = MachineStatus.TRANSACTION_ERROR;
            throw exception;
        }
        // After any successful provider tick its live handler is authoritative, including clears.
        savedDisplayRecipe = null;
        if (!isStructureValid) { status = MachineStatus.INVALID_STRUCTURE; return; }
        ModRecipe<?> active = recipeHandler.orElse(null);
        if (active == null) {
            observedRecipe = null; observedRolls = null;
            status = before > 0 ? MachineStatus.PROCESSING : throttledDiagnosis(level, type, inputs, fluids);
            return;
        }
        if (active != observedRecipe) {
            // Serialize once per new/restored transaction to read the provider's exact persisted
            // chance decisions. Never reroll, inspect private fields, or serialize every tick.
            observedRolls = persistedOutputIndexes(level);
            observedRecipe = active;
        }
        if (!ingredientsConsumed || energyConsumed >= recipeEnergyCost) {
            int[] rolls = observedRolls == null ? new int[0] : observedRolls;
            ModRecipeData data = active.getRecipe();
            ModRecipe<?> itemsOnly = outputProbe(active, data, false);
            if (!itemsOnly.canFitOutputs(outputs, List.of(), rolls)) { status = MachineStatus.ITEM_OUTPUT_BLOCKED; return; }
            ModRecipe<?> fluidsOnly = outputProbe(active, data, true);
            if (!fluidsOnly.canFitOutputs(null, outputFluids, new int[0])) { status = MachineStatus.FLUID_OUTPUT_BLOCKED; return; }
        }
        status = energyConsumed > before || energyConsumed >= recipeEnergyCost
                ? MachineStatus.PROCESSING : MachineStatus.INSUFFICIENT_ENERGY;
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ModRecipe<?> outputProbe(ModRecipe<?> recipe, ModRecipeData data, boolean fluid) {
        return (ModRecipe<?>) ((ModRecipe) recipe).create(recipe.getId(), new ModRecipeData(
                data.getIngredientItems(), fluid ? new OutputItem[0] : data.getOutputItems(),
                data.getFluidIngredients(), fluid ? data.getFluidOutputs() : new FluidStack[0], data.getTotalEnergy()));
    }
    /** Diagnostics are informational: never rescan every recipe on every idle server tick. */
    private <R extends ModRecipe<R>> MachineStatus throttledDiagnosis(Level level, RecipeType<R> type,
            IItemHandler inputs, List<? extends IFluidHandler> fluids) {
        long tick = level.getGameTime();
        if (DiagnosticThrottle.shouldRefresh(tick, lastDiagnosisTick)) {
            cachedDiagnosis = diagnoseInputs(level, type, inputs, fluids);
            lastDiagnosisTick = tick;
        }
        return cachedDiagnosis;
    }
    private <R extends ModRecipe<R>> MachineStatus diagnoseInputs(Level level, RecipeType<R> type,
            IItemHandler inputs, List<? extends IFluidHandler> fluids) {
        boolean anyItems = false;
        for (int slot = 0; slot < inputs.getSlots(); slot++) anyItems |= !inputs.getStackInSlot(slot).isEmpty();
        boolean insufficientQuantity = false;
        boolean hasRecipes = false;
        for (R recipe : diagnosticRecipes(level, type)) {
            hasRecipes = true;
            ModRecipeData data = recipe.getRecipe();
            R itemsOnly = recipe.create(recipe.getId(), new ModRecipeData(data.getIngredientItems(),
                    data.getOutputItems(), new FluidStack[0], data.getFluidOutputs(), data.getTotalEnergy()));
            if (RecipePreflight.matchesInputs(itemsOnly, inputs, List.of())) return MachineStatus.MISSING_FLUID;
            IngredientItem[] minimal = new IngredientItem[data.getIngredientItems().length];
            for (int i = 0; i < minimal.length; i++) {
                IngredientItem original = data.getIngredientItems()[i];
                var stack = original.getItemStack().copy();
                stack.setCount(1);
                minimal[i] = new IngredientItem(stack, original.isConsumable());
            }
            R minimalItems = recipe.create(recipe.getId(), new ModRecipeData(minimal,
                    data.getOutputItems(), new FluidStack[0], data.getFluidOutputs(), data.getTotalEnergy()));
            insufficientQuantity |= RecipePreflight.matchesInputs(minimalItems, inputs, List.of());
        }
        return !hasRecipes || (anyItems && !insufficientQuantity) ? MachineStatus.NO_MATCHING_RECIPE : MachineStatus.MISSING_ITEMS;
    }
}
