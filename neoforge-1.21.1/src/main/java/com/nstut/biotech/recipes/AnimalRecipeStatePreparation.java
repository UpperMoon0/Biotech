package com.nstut.biotech.recipes;

import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.MobItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public final class AnimalRecipeStatePreparation {
    private AnimalRecipeStatePreparation() {
    }

    public static BreedingChamberRecipe prepareBreeding(BreedingChamberRecipe recipe, IItemHandler inputs) {
        PreparedAnimalInputs.Selection selection = PreparedAnimalInputs.select(recipe, inputs);
        if (selection.animals().isEmpty()) return recipe;
        ItemStack donor = selection.animals().get(0).getItemStack();
        ModRecipeData prepared = selection.applyTo(recipe.getRecipe());
        applyState(prepared, donor, CapturedAnimalStackState.forOffspring(donor));
        return recipe.create(recipe.getId(), prepared);
    }

    public static TerrestrialHabitatRecipe prepareGrowth(TerrestrialHabitatRecipe recipe, IItemHandler inputs) {
        return prepareHabitat(recipe, inputs);
    }

    public static TerrestrialHabitatRecipe prepareHabitat(TerrestrialHabitatRecipe recipe, IItemHandler inputs) {
        PreparedAnimalInputs.Selection selection = PreparedAnimalInputs.select(recipe, inputs);
        if (selection.animals().isEmpty()) return recipe;
        ModRecipeData prepared = selection.applyTo(recipe.getRecipe());
        List<ItemStack> individuals = new ArrayList<>();
        for (IngredientItem selected : selection.animals()) {
            if (!selected.isConsumable()) continue;
            for (int count = 0; count < selected.getItemStack().getCount(); count++) {
                ItemStack individual = selected.getItemStack().copy();
                individual.setCount(1);
                individuals.add(individual);
            }
        }
        // Each consumed individual can donate its full payload once. In particular a count-three
        // output must not clone the first lamb's name, variant, or inventory over all three adults.
        List<OutputItem> outputs = new ArrayList<>();
        // Resolve the original output groups once, before splitting their distinct state payloads.
        // All members of a probabilistic batch therefore still succeed or fail together, and the
        // snapshot contains the decision so blocked ticks and reloads cannot reroll it.
        for (int outputIndex : recipe.rollItemOutputIndexes()) {
            OutputItem output = prepared.getOutputItems()[outputIndex];
            ItemStack template = output.getItemStack();
            if (!(template.getItem() instanceof MobItem) && !(template.getItem() instanceof CapturedAnimalItem)) {
                appendResolvedOutput(outputs, template);
                continue;
            }
            for (int count = 0; count < template.getCount(); count++) {
                ItemStack grown = template.copy();
                grown.setCount(1);
                for (int index = 0; index < individuals.size(); index++) {
                    ItemStack source = individuals.get(index);
                    if (!isAnimalOutputFor(grown, CapturedAnimalStackState.entityTypeId(source))) continue;
                    CapturedAnimalStackState.writeDerived(grown, source, CapturedAnimalStackState.forAdult(source));
                    individuals.remove(index);
                    break;
                }
                appendResolvedOutput(outputs, grown);
            }
        }
        prepared = new ModRecipeData(prepared.getIngredientItems(), outputs.toArray(OutputItem[]::new),
                prepared.getFluidIngredients(), prepared.getFluidOutputs(), prepared.getTotalEnergy());
        ItemStack donor = selection.animals().get(0).getItemStack();
        applySheepWoolColor(prepared, donor, CapturedAnimalStackState.read(donor));
        return recipe.create(recipe.getId(), prepared);
    }

    /** Merge only identical complete payloads after resolving their original chance groups. */
    private static void appendResolvedOutput(List<OutputItem> outputs, ItemStack stack) {
        ItemStack remaining = stack.copy();
        int limit = Math.min(99, remaining.getMaxStackSize());
        for (OutputItem output : outputs) {
            ItemStack existing = output.getItemStack();
            if (!ItemStack.isSameItemSameComponents(existing, remaining)) continue;
            int moved = Math.min(remaining.getCount(), Math.max(0, limit - existing.getCount()));
            ItemStack merged = existing.copy();
            merged.grow(moved);
            // 26.1 RecipeItem also caches a serialization template; always refresh via its setter.
            output.setItemStack(merged);
            remaining.shrink(moved);
            if (remaining.isEmpty()) return;
        }
        while (!remaining.isEmpty()) {
            if (outputs.size() >= 256) {
                // The provider persists at most 256 entries. Abort before startRecipe/consumption
                // rather than create an in-flight transaction that cannot survive a save/reload.
                throw new IllegalStateException("Prepared animal outputs exceed the snapshot entry limit");
            }
            ItemStack part = remaining.copy();
            part.setCount(Math.min(limit, remaining.getCount()));
            outputs.add(new OutputItem(part, 1.0f));
            remaining.shrink(part.getCount());
        }
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
        if (state.contains("Color")) {
            return Byte.toUnsignedInt(state.getByte("Color"));
        }
        CompoundTag root = donor.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        return root.contains("SheepColor") ? root.getInt("SheepColor") : 0;
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
