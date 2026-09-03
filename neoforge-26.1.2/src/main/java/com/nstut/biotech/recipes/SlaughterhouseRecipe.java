package com.nstut.biotech.recipes;

import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.RecipeSerializerFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class SlaughterhouseRecipe extends ModRecipe<SlaughterhouseRecipe> {
    public static final RecipeSerializer<SlaughterhouseRecipe> SERIALIZER =
            new RecipeSerializerFactory<SlaughterhouseRecipe>().createSerializer(SlaughterhouseRecipe::new);

    public static final RecipeType<SlaughterhouseRecipe> TYPE = new RecipeType<>() {
    };

    public SlaughterhouseRecipe(Identifier id, ModRecipeData recipe) {
        super(id, recipe, SERIALIZER, TYPE);
    }

    @Override
    protected SlaughterhouseRecipe createInstance(Identifier id, ModRecipeData recipe) {
        return new SlaughterhouseRecipe(id, recipe);
    }
}
