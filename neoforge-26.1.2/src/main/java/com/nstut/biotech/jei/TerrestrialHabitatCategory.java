package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.TerrestrialHabitatRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TerrestrialHabitatCategory implements IRecipeCategory<TerrestrialHabitatRecipe> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, MachineRegistries.TERRESTRIAL_HABITAT.id());
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/jei/" + MachineRegistries.TERRESTRIAL_HABITAT.id() + ".png");
    public static final RecipeType<TerrestrialHabitatRecipe> TYPE = new RecipeType<>(UID, TerrestrialHabitatRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public TerrestrialHabitatCategory(IGuiHelper helper) {
        background = helper.createDrawable(TEXTURE, 0, 0, 140, 52);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MachineRegistries.TERRESTRIAL_HABITAT.blockItem().get()));
    }

    @Override public @NotNull RecipeType<TerrestrialHabitatRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.TERRESTRIAL_HABITAT.id()); }
    @Override public int getWidth() { return background.getWidth(); }
    @Override public int getHeight() { return background.getHeight(); }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull TerrestrialHabitatRecipe recipe, @NotNull IFocusGroup focuses) {
        List<ItemStack> ingredients = recipe.getItemIngredients().stream().map(v -> v.getItemStack()).toList();
        FluidStack fluid = recipe.getFluidIngredients().get(0);
        List<ItemStack> outputs = recipe.getItemOutputs().stream().map(v -> v.getItemStack()).toList();

        builder.addSlot(RecipeIngredientRole.INPUT, 23, 1).addItemStack(ingredients.get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 41, 1).addItemStack(ingredients.get(1));
        builder.addSlot(RecipeIngredientRole.INPUT, 32, 21)
                .addFluidStack(fluid.getFluid(), fluid.getAmount())
                .setFluidRenderer(fluid.getAmount(), false, 16, 16);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 11).addItemStack(outputs.get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 119, 11).addItemStack(outputs.get(1));
    }

    @Override
    public void draw(TerrestrialHabitatRecipe recipe, @NotNull IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        background.draw(graphics);
        graphics.text(Minecraft.getInstance().font, "Energy: " + recipe.getTotalEnergy() + " FE", 0, 42, 4210752, false);
    }
}
