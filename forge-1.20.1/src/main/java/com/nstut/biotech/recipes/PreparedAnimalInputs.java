package com.nstut.biotech.recipes;

import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.MobItem;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.RecipeTransactionException;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction-local, persisted binding between prepared outputs and their concrete animal inputs.
 * The marker lives only on recipe ingredient copies, never on player inventory stacks. Since it is
 * ordinary stack data it survives ModRecipeData's network and active-transaction persistence codecs.
 */
public final class PreparedAnimalInputs {
    private static final String BOUND_TAG = "BiotechPreparedAnimalInput";

    private PreparedAnimalInputs() {}

    public static ModRecipeData bind(AnimalMobRecipe<?> recipe, IItemHandler inputs, ModRecipeData prepared) {
        return select(recipe, inputs).applyTo(prepared);
    }

    public static boolean isBound(ItemStack stack) {
        CompoundTag root = stack.getTag();
        return root != null && root.contains(BOUND_TAG);
    }

    public static boolean matchesBound(ItemStack required, ItemStack present) {
        return !required.isEmpty() && !present.isEmpty()
                && ItemStack.isSameItemSameTags(withoutMarker(required), withoutMarker(present));
    }

    private static ItemStack withoutMarker(ItemStack stack) {
        ItemStack copy = stack.copy();
        CompoundTag root = copy.getTag();
        if (root != null) {
            root.remove(BOUND_TAG);
            if (root.isEmpty()) copy.setTag(null);
        }
        return copy;
    }

    /**
     * Find a complete allocation, including overlapping any/adult/baby and legacy/generic selectors.
     * An augmenting path can move an earlier flexible requirement away from a later constrained
     * donor. Rebuild in authored ingredient and inventory-slot order for deterministic inheritance.
     */
    static Selection select(AnimalMobRecipe<?> recipe, IItemHandler inputs) {
        List<IngredientItem> authored = recipe.getItemIngredients();
        List<IngredientItem> requirements = authored.stream()
                .filter(ingredient -> isAnimal(ingredient.getItemStack())).toList();
        int slots = inputs.getSlots();
        int[] available = new int[slots];
        for (int slot = 0; slot < slots; slot++) available[slot] = inputs.getStackInSlot(slot).getCount();
        boolean[][] matches = new boolean[requirements.size()][slots];
        int[][] allocated = new int[requirements.size()][slots];
        for (int animal = 0; animal < requirements.size(); animal++) {
            for (int slot = 0; slot < slots; slot++) {
                matches[animal][slot] = recipe.matchesAnimalInput(requirements.get(animal).getItemStack(), inputs.getStackInSlot(slot));
            }
        }
        for (int animal = 0; animal < requirements.size(); animal++) {
            for (int unit = 0; unit < requirements.get(animal).getItemStack().getCount(); unit++) {
                if (!AnimalInputAllocation.allocateUnit(animal, matches, allocated, available,
                        new boolean[requirements.size()], new boolean[slots])) {
                    // The provider's semantic aggregation can overaccept overlapping selectors.
                    // Its controller catches this safely before installing/consuming a transaction.
                    throw new RecipeTransactionException("No complete allocation for prepared animal inputs");
                }
            }
        }
        List<IngredientItem> bound = new ArrayList<>();
        List<IngredientItem> animals = new ArrayList<>();
        int animal = 0;
        for (IngredientItem ingredient : authored) {
            if (!isAnimal(ingredient.getItemStack())) {
                bound.add(new IngredientItem(ingredient.getItemStack().copy(), ingredient.isConsumable()));
                continue;
            }
            for (int slot = 0; slot < slots; slot++) {
                int count = allocated[animal][slot];
                if (count == 0) continue;
                ItemStack selected = inputs.getStackInSlot(slot).copy();
                selected.setCount(count);
                animals.add(new IngredientItem(selected.copy(), ingredient.isConsumable()));
                ItemStack stack = selected;
                stack.getOrCreateTag().putBoolean(BOUND_TAG, true);
                appendBoundInput(bound, stack, ingredient.isConsumable());
            }
            animal++;
        }
        return new Selection(bound, animals);
    }

    private static boolean isAnimal(ItemStack stack) {
        return stack.getItem() instanceof MobItem || stack.getItem() instanceof CapturedAnimalItem;
    }

    private static void appendBoundInput(List<IngredientItem> bound, ItemStack stack, boolean consumable) {
        ItemStack remaining = stack.copy();
        int limit = Math.min(99, remaining.getMaxStackSize());
        // Preserve the first occurrence of each donor. Only equivalent payloads with the same
        // consumption policy may merge; retained parents must never become consumable inputs.
        for (IngredientItem ingredient : bound) {
            ItemStack existing = ingredient.getItemStack();
            if (ingredient.isConsumable() != consumable || !isBound(existing)
                    || !matchesBound(existing, remaining)) continue;
            int moved = Math.min(remaining.getCount(), Math.max(0, limit - existing.getCount()));
            ItemStack merged = existing.copy();
            merged.grow(moved);
            // Keep the modern RecipeItem's persisted ItemStackTemplate in sync with runtime count.
            ingredient.setItemStack(merged);
            remaining.shrink(moved);
            if (remaining.isEmpty()) return;
        }
        while (!remaining.isEmpty()) {
            ItemStack part = remaining.copy();
            part.setCount(Math.min(limit, remaining.getCount()));
            bound.add(new IngredientItem(part, consumable));
            remaining.shrink(part.getCount());
        }
    }

    record Selection(List<IngredientItem> ingredients, List<IngredientItem> animals) {
        ModRecipeData applyTo(ModRecipeData prepared) {
            if (ingredients.size() > 256 || prepared.getOutputItems().length > 256) {
                throw new RecipeTransactionException("Prepared animal transaction exceeds the snapshot entry limit");
            }
            ModRecipeData copy = prepared.copy();
            return new ModRecipeData(ingredients.toArray(IngredientItem[]::new), copy.getOutputItems(),
                    copy.getFluidIngredients(), copy.getFluidOutputs(), copy.getTotalEnergy());
        }
    }
}
