package com.nstut.biotech.recipes;

import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.RecipeSerializerFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class BreedingChamberRecipe extends ModRecipe<BreedingChamberRecipe> {
    public static final RecipeSerializer<BreedingChamberRecipe> SERIALIZER =
            new RecipeSerializerFactory<BreedingChamberRecipe>().createSerializer(BreedingChamberRecipe::new);

    public static final RecipeType<BreedingChamberRecipe> TYPE = new RecipeType<>(){};

    public BreedingChamberRecipe(ResourceLocation id, ModRecipeData recipe) {
        super(id, recipe, SERIALIZER, TYPE);
    }

    @Override
    protected BreedingChamberRecipe createInstance(ResourceLocation id, ModRecipeData recipe) {
        return new BreedingChamberRecipe(id, recipe);
    }
}