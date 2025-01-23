package com.nstut.biotech.recipes;

import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.RecipeSerializerFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class MixerRecipe extends ModRecipe<MixerRecipe> {
    public static final RecipeSerializer<MixerRecipe> SERIALIZER =
            new RecipeSerializerFactory<MixerRecipe>().createSerializer(MixerRecipe::new);

    public static final RecipeType<MixerRecipe> TYPE = new RecipeType<>() {
    };

    public MixerRecipe(ResourceLocation id, ModRecipeData recipe) {
        super(id, recipe, SERIALIZER, TYPE);
    }

    @Override
    protected MixerRecipe createInstance(ResourceLocation id, ModRecipeData recipe) {
        return new MixerRecipe(id, recipe);
    }
}