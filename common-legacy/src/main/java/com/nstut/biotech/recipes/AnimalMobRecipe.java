package com.nstut.biotech.recipes;

import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.MobItem;
import com.nstut.nstutlib.recipes.InputAwareRecipeSnapshot;
import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/** Shared recipe semantics for machines that consume captured animal items. */
public abstract class AnimalMobRecipe<T extends ModRecipe<T>> extends ModRecipe<T>
        implements InputAwareRecipeSnapshot {
    protected AnimalMobRecipe(ResourceLocation id, ModRecipeData recipe,
                              RecipeSerializer<T> serializer, RecipeType<T> type) {
        super(id, recipe, serializer, type);
    }

    @Override
    public ModRecipeData snapshotForExecution(List<ItemStack> itemInputs) {
        ModRecipeData snapshot = recipe.copy();
        specializeExecutionSnapshot(snapshot, itemInputs);
        return snapshot;
    }

    /** Subclasses may rewrite copied outputs from concrete input state without mutating datapack recipes. */
    protected void specializeExecutionSnapshot(ModRecipeData snapshot, List<ItemStack> itemInputs) {
    }

    protected final ItemStack firstMatchingAnimalInput(List<ItemStack> itemInputs) {
        if (recipe.getIngredientItems().length == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack required = recipe.getIngredientItems()[0].getItemStack();
        for (ItemStack present : itemInputs) {
            if (!present.isEmpty() && itemIngredientsMatch(required, present)) {
                return present.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    protected final void setCapturedStateOnOutput(ModRecipeData snapshot,
                                                   int outputIndex,
                                                   CompoundTag state) {
        if (outputIndex < 0 || outputIndex >= snapshot.getOutputItems().length) {
            return;
        }
        ItemStack output = snapshot.getOutputItems()[outputIndex].getItemStack().copy();
        if (output.isEmpty()) {
            return;
        }
        CapturedAnimalStackState.write(output, state);
        snapshot.getOutputItems()[outputIndex].setItemStack(output);
    }

    @Override
    protected boolean itemIngredientsMatch(ItemStack required, ItemStack present) {
        if (!required.isEmpty()
                && !present.isEmpty()
                && required.getItem() instanceof MobItem
                && present.getItem() instanceof MobItem
                && required.is(present.getItem())) {
            return true;
        }

        if (!required.isEmpty()
                && !present.isEmpty()
                && required.getItem() instanceof MobItem requiredMob
                && present.getItem() instanceof CapturedAnimalItem
                && requiredMob.entityType() != null
                && CapturedAnimalItem.matchesLegacyVariant(
                        present, requiredMob.entityType(), requiredMob.isBabyVariant())) {
            return true;
        }

        return super.itemIngredientsMatch(required, present);
    }
}
