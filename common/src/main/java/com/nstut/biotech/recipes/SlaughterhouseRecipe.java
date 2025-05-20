package com.nstut.biotech.recipes;

import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.RecipeSerializerFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class SlaughterhouseRecipe extends ModRecipe<SlaughterhouseRecipe> {
    public static final RecipeSerializer<SlaughterhouseRecipe> SERIALIZER =
            new RecipeSerializerFactory<SlaughterhouseRecipe>().createSerializer(SlaughterhouseRecipe::new);

    public static final RecipeType<SlaughterhouseRecipe> TYPE = new RecipeType<>() {
    };

    public SlaughterhouseRecipe(ResourceLocation id, ModRecipeData recipe) {
        super(id, recipe, SERIALIZER, TYPE);
    }

    @Override
    protected SlaughterhouseRecipe createInstance(ResourceLocation id, ModRecipeData recipe) {
        return new SlaughterhouseRecipe(id, recipe);
    }
}
