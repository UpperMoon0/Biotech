package com.nstut.biotech.recipes;

import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.MobItem;
import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Shared recipe semantics for machines that consume captured animal items. */
public abstract class AnimalMobRecipe<T extends ModRecipe<T>> extends ModRecipe<T> {
    protected AnimalMobRecipe(ResourceLocation id, ModRecipeData recipe,
                              RecipeSerializer<T> serializer, RecipeType<T> type) {
        super(id, recipe, serializer, type);
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

        if (!required.isEmpty()
                && !present.isEmpty()
                && required.getItem() instanceof CapturedAnimalItem
                && present.getItem() instanceof CapturedAnimalItem) {
            return CapturedAnimalItem.matchesGenericSpecies(required, present);
        }

        return super.itemIngredientsMatch(required, present);
    }
}
