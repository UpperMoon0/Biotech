package com.nstut.biotech.recipes;

import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.MobItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public final class AnimalRecipeStatePreparation {
    private AnimalRecipeStatePreparation() {
    }

    public static BreedingChamberRecipe prepareBreeding(BreedingChamberRecipe recipe, IItemHandler inputs) {
        return prepare(recipe, inputs, true);
    }

    public static TerrestrialHabitatRecipe prepareGrowth(TerrestrialHabitatRecipe recipe, IItemHandler inputs) {
        return prepare(recipe, inputs, false);
    }

    private static BreedingChamberRecipe prepare(BreedingChamberRecipe recipe, IItemHandler inputs, boolean offspring) {
        ItemStack donor = findDonor(recipe, inputs);
        if (donor.isEmpty() || CapturedAnimalStackState.read(donor).isEmpty()) {
            return recipe;
        }

        ModRecipeData prepared = recipe.getRecipe().copy();
        applyState(prepared, donor, offspring ? CapturedAnimalStackState.forOffspring(donor) : CapturedAnimalStackState.forAdult(donor));
        return recipe.create(recipe.getId(), prepared);
    }

    private static TerrestrialHabitatRecipe prepare(TerrestrialHabitatRecipe recipe, IItemHandler inputs, boolean offspring) {
        ItemStack donor = findDonor(recipe, inputs);
        if (donor.isEmpty() || CapturedAnimalStackState.read(donor).isEmpty()) {
            return recipe;
        }

        ModRecipeData prepared = recipe.getRecipe().copy();
        applyState(prepared, donor, offspring ? CapturedAnimalStackState.forOffspring(donor) : CapturedAnimalStackState.forAdult(donor));
        return recipe.create(recipe.getId(), prepared);
    }

    private static ItemStack findDonor(AnimalMobRecipe<?> recipe, IItemHandler inputs) {
        for (var ingredient : recipe.getItemIngredients()) {
            ItemStack required = ingredient.getItemStack();
            if (!(required.getItem() instanceof MobItem) && !(required.getItem() instanceof CapturedAnimalItem)) {
                continue;
            }
            for (int slot = 0; slot < inputs.getSlots(); slot++) {
                ItemStack present = inputs.getStackInSlot(slot);
                if (recipe.matchesAnimalInput(required, present)) {
                    return present.copy();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static void applyState(ModRecipeData prepared, ItemStack donor, CompoundTag derivedState) {
        String donorType = CapturedAnimalStackState.entityTypeId(donor);
        for (OutputItem output : prepared.getOutputItems()) {
            ItemStack stack = output.getItemStack().copy();
            if (!isAnimalOutputFor(stack, donorType)) {
                continue;
            }
            CapturedAnimalStackState.writeDerived(stack, donor, derivedState);
            output.setItemStack(stack);
        }
    }

    private static boolean isAnimalOutputFor(ItemStack stack, String donorType) {
        if (stack.getItem() instanceof MobItem mobItem) {
            return mobItem.entityType() != null
                    && donorType.equals(net.minecraft.world.entity.EntityType.getKey(mobItem.entityType()).toString());
        }
        if (stack.getItem() instanceof CapturedAnimalItem) {
            String outputType = CapturedAnimalStackState.entityTypeId(stack);
            return outputType.isEmpty() || outputType.equals(donorType);
        }
        return false;
    }
}
