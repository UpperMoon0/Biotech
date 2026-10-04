package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.GreenhouseRecipe;
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

public class GreenhouseCategory implements IRecipeCategory<GreenhouseRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Biotech.MOD_ID, MachineRegistries.GREENHOUSE.id());
    public static final RecipeType<GreenhouseRecipe> TYPE = new RecipeType<>(UID, GreenhouseRecipe.class);
    private final IDrawable icon;

    public GreenhouseCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(MachineRegistries.GREENHOUSE.blockItem().get());
    }

    @Override public @NotNull RecipeType<GreenhouseRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.GREENHOUSE.id()); }
    @Override public int getWidth() { return JeiRecipeLayout.WIDTH; }
    @Override public int getHeight() { return JeiRecipeLayout.HEIGHT; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull GreenhouseRecipe recipe, @NotNull IFocusGroup focuses) {
        JeiMachineRecipeLayout.addSlots(builder, recipe);
    }

    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull GreenhouseRecipe recipe, @NotNull IFocusGroup focuses) {
        JeiMachineRecipeLayout.addExtras(builder, recipe);
    }
}
