package com.nstut.biotech.jei;

import mezz.jei.api.recipe.RecipeIngredientRole;

/** JEI 20 renamed the non-consumable catalyst role to CRAFTING_STATION. */
public final class JeiIngredientRoles {
    private JeiIngredientRoles() { }

    public static RecipeIngredientRole input(boolean consumable) {
        return consumable ? RecipeIngredientRole.INPUT : RecipeIngredientRole.CATALYST;
    }
}
