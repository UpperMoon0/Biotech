package com.nstut.biotech.recipes;

import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.MobItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;

public final class AnimalRecipeStatePreparation {
    private AnimalRecipeStatePreparation() {
    }

    public static BreedingChamberRecipe prepareBreeding(BreedingChamberRecipe recipe, IItemHandler inputs) {
        return prepare(recipe, inputs, true);
    }

    public static TerrestrialHabitatRecipe prepareGrowth(TerrestrialHabitatRecipe recipe, IItemHandler inputs) {
        return prepareHabitat(recipe, inputs);
    }

    public static TerrestrialHabitatRecipe prepareHabitat(TerrestrialHabitatRecipe recipe, IItemHandler inputs) {
        ItemStack donor = findDonor(recipe, inputs);
        if (donor.isEmpty()) {
            return recipe;
        }

        ModRecipeData prepared = recipe.getRecipe().copy();
        CompoundTag state = CapturedAnimalStackState.read(donor);
        if (!state.isEmpty()) {
            applyState(prepared, donor, CapturedAnimalStackState.forAdult(donor));
        }
        applySheepWoolColor(prepared, donor, state);
        return recipe.create(recipe.getId(), prepared);
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

    private static void applySheepWoolColor(ModRecipeData prepared, ItemStack donor, CompoundTag state) {
        if (!"minecraft:sheep".equals(CapturedAnimalStackState.entityTypeId(donor))) {
            return;
        }
        int colorId = sheepColorId(donor, state);
        Item wool = woolFor(DyeColor.byId(colorId));
        for (OutputItem output : prepared.getOutputItems()) {
            ItemStack stack = output.getItemStack();
            if (!stack.is(Items.WHITE_WOOL)) {
                continue;
            }
            output.setItemStack(new ItemStack(wool, stack.getCount()));
        }
    }

    private static int sheepColorId(ItemStack donor, CompoundTag state) {
        int capturedColor = state.getByte("Color").map(Byte::toUnsignedInt).orElse(-1);
        if (capturedColor >= 0) {
            return capturedColor;
        }
        CompoundTag root = donor.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        return root.getInt("SheepColor").orElse(0);
    }

    private static Item woolFor(DyeColor color) {
        return switch (color) {
            case WHITE -> Items.WHITE_WOOL;
            case ORANGE -> Items.ORANGE_WOOL;
            case MAGENTA -> Items.MAGENTA_WOOL;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
            case YELLOW -> Items.YELLOW_WOOL;
            case LIME -> Items.LIME_WOOL;
            case PINK -> Items.PINK_WOOL;
            case GRAY -> Items.GRAY_WOOL;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
            case CYAN -> Items.CYAN_WOOL;
            case PURPLE -> Items.PURPLE_WOOL;
            case BLUE -> Items.BLUE_WOOL;
            case BROWN -> Items.BROWN_WOOL;
            case GREEN -> Items.GREEN_WOOL;
            case RED -> Items.RED_WOOL;
            case BLACK -> Items.BLACK_WOOL;
        };
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
