package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.BreedingChamberRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BreedingChamberCategory implements IRecipeCategory<BreedingChamberRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Biotech.MOD_ID, MachineRegistries.BREEDING_CHAMBER.id());
    public static final RecipeType<BreedingChamberRecipe> TYPE = new RecipeType<>(UID, BreedingChamberRecipe.class);
    private final IDrawable icon;

    public BreedingChamberCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(MachineRegistries.BREEDING_CHAMBER.blockItem().get());
    }

    @Override public @NotNull RecipeType<BreedingChamberRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.BREEDING_CHAMBER.id()); }
    @Override public int getWidth() { return JeiRecipeLayout.WIDTH; }
    @Override public int getHeight() { return JeiRecipeLayout.HEIGHT; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BreedingChamberRecipe recipe, @NotNull IFocusGroup focuses) {
        JeiMachineRecipeLayout.addSlots(builder, recipe);
    }

    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull BreedingChamberRecipe recipe, @NotNull IFocusGroup focuses) {
        JeiMachineRecipeLayout.addExtras(builder, recipe);
    }
}
