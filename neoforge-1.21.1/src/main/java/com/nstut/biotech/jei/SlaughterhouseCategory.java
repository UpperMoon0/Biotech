package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.SlaughterhouseRecipe;
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

public class SlaughterhouseCategory implements IRecipeCategory<SlaughterhouseRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, MachineRegistries.SLAUGHTERHOUSE.id());
    public static final RecipeType<SlaughterhouseRecipe> TYPE = new RecipeType<>(UID, SlaughterhouseRecipe.class);
    private final IDrawable icon;

    public SlaughterhouseCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(MachineRegistries.SLAUGHTERHOUSE.blockItem().get());
    }

    @Override public @NotNull RecipeType<SlaughterhouseRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.SLAUGHTERHOUSE.id()); }
    @Override public int getWidth() { return JeiRecipeLayout.WIDTH; }
    @Override public int getHeight() { return JeiRecipeLayout.HEIGHT; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SlaughterhouseRecipe recipe, @NotNull IFocusGroup focuses) {
        JeiMachineRecipeLayout.addSlots(builder, recipe);
    }

    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull SlaughterhouseRecipe recipe, @NotNull IFocusGroup focuses) {
        JeiMachineRecipeLayout.addExtras(builder, recipe);
    }
}
