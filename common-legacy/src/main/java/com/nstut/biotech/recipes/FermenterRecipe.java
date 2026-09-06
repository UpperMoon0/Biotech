package com.nstut.biotech.recipes;

import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.RecipeSerializerFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class FermenterRecipe extends ModRecipe<FermenterRecipe> {
    public static final RecipeSerializer<FermenterRecipe> SERIALIZER =
            new RecipeSerializerFactory<FermenterRecipe>().createSerializer(FermenterRecipe::new);

    public static final RecipeType<FermenterRecipe> TYPE = new RecipeType<>() {
    };

    public FermenterRecipe(ResourceLocation id, ModRecipeData recipe) {
        super(id, recipe, SERIALIZER, TYPE);
    }

    @Override
    protected FermenterRecipe createInstance(ResourceLocation id, ModRecipeData recipe) {
        return new FermenterRecipe(id, recipe);
    }
}