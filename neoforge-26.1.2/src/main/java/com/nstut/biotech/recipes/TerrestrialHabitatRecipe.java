package com.nstut.biotech.recipes;

import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.RecipeSerializerFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class TerrestrialHabitatRecipe extends ModRecipe<TerrestrialHabitatRecipe> {
    public static final RecipeSerializer<TerrestrialHabitatRecipe> SERIALIZER =
            new RecipeSerializerFactory<TerrestrialHabitatRecipe>().createSerializer(TerrestrialHabitatRecipe::new);

    public static final RecipeType<TerrestrialHabitatRecipe> TYPE = new RecipeType<>(){};

    public TerrestrialHabitatRecipe(ResourceLocation id, ModRecipeData recipe) {
        super(id, recipe, SERIALIZER, TYPE);
    }

    @Override
    protected TerrestrialHabitatRecipe createInstance(ResourceLocation id, ModRecipeData recipe) {
        return new TerrestrialHabitatRecipe(id, recipe);
    }
}