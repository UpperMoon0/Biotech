package com.nstut.biotech.recipes;

import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.RecipeSerializerFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class GreenhouseRecipe extends ModRecipe<GreenhouseRecipe> {
    public static final RecipeSerializer<GreenhouseRecipe> SERIALIZER =
            new RecipeSerializerFactory<GreenhouseRecipe>().createSerializer(GreenhouseRecipe::new);

    public static final RecipeType<GreenhouseRecipe> TYPE = new RecipeType<>() {
    };

    public GreenhouseRecipe(ResourceLocation id, ModRecipeData recipe) {
        super(id, recipe, SERIALIZER, TYPE);
    }

    @Override
    protected GreenhouseRecipe createInstance(ResourceLocation id, ModRecipeData recipe) {
        return new GreenhouseRecipe(id, recipe);
    }
}
