package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.TerrestrialHabitatRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class TerrestrialHabitatCategory implements IRecipeCategory<TerrestrialHabitatRecipe> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, MachineRegistries.TERRESTRIAL_HABITAT.id());
    public static final RecipeType<TerrestrialHabitatRecipe> TYPE = new RecipeType<>(UID, TerrestrialHabitatRecipe.class);
    private final IDrawable icon;

    public TerrestrialHabitatCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(MachineRegistries.TERRESTRIAL_HABITAT.blockItem().get());
    }

    @Override public @NotNull RecipeType<TerrestrialHabitatRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.TERRESTRIAL_HABITAT.id()); }
    @Override public int getWidth() { return JeiRecipeLayout.WIDTH; }
    @Override public int getHeight() { return JeiRecipeLayout.HEIGHT; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull TerrestrialHabitatRecipe recipe, @NotNull IFocusGroup focuses) {
        JeiMachineRecipeLayout.addSlots(builder, recipe);
    }

    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull TerrestrialHabitatRecipe recipe, @NotNull IFocusGroup focuses) {
        JeiMachineRecipeLayout.addExtras(builder, recipe);
    }
}
