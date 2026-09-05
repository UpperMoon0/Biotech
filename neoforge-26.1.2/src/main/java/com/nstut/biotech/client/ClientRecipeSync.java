package com.nstut.biotech.client;

import net.minecraft.world.item.crafting.RecipeMap;

public final class ClientRecipeSync {
    private static volatile RecipeMap recipes = RecipeMap.EMPTY;

    private ClientRecipeSync() {}

    public static RecipeMap get() {
        return recipes;
    }

    public static void set(RecipeMap recipeMap) {
        recipes = recipeMap == null ? RecipeMap.EMPTY : recipeMap;
    }

    public static void clear() {
        recipes = RecipeMap.EMPTY;
    }
}
