package com.nstut.biotech.recipes;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class ArborariumRecipe extends ModRecipe<ArborariumRecipe> {

    public static final RecipeSerializer<ArborariumRecipe> SERIALIZER = new Serializer();
    public static final RecipeType<ArborariumRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "arborarium";
        }
    };

    public ArborariumRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(Level pLevel) {
        return false; // Placeholder, no actual matching logic needed for now
    }

    @Override
    public ItemStack assemble() {
        return ItemStack.EMPTY; // Placeholder
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY; // Placeholder
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    private static class Serializer implements RecipeSerializer<ArborariumRecipe> {
        @Override
        public ArborariumRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            return new ArborariumRecipe(pRecipeId);
        }

        @Override
        public @Nullable ArborariumRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            return new ArborariumRecipe(pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ArborariumRecipe pRecipe) {
            // No data to write for this placeholder recipe
        }
    }
}