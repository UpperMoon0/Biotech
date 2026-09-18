package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.GreenhouseRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GreenhouseCategory implements IRecipeCategory<GreenhouseRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Biotech.MOD_ID, MachineRegistries.GREENHOUSE.id());
    private static final int WIDTH = 153;
    private static final int HEIGHT = 52;
    public static final RecipeType<GreenhouseRecipe> TYPE = new RecipeType<>(UID, GreenhouseRecipe.class);
    private final IDrawable arrow;
    private final IDrawable icon;

    public GreenhouseCategory(IGuiHelper helper) {
        arrow = helper.getRecipeArrow();
        icon = helper.createDrawableItemLike(MachineRegistries.GREENHOUSE.blockItem().get());
    }

    @Override public @NotNull RecipeType<GreenhouseRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.GREENHOUSE.id()); }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull GreenhouseRecipe recipe, @NotNull IFocusGroup focuses) {
        List<Ingredient> itemInputs = recipe.getItemIngredients().stream().map(v -> Ingredient.of(v.getItemStack())).toList();
        List<Ingredient> itemOutputs = recipe.getItemOutputs().stream().map(v -> Ingredient.of(v.getItemStack())).toList();
        for (int i = 0; i < itemInputs.size(); i++)
            builder.addSlot(RecipeIngredientRole.INPUT, 23 + (i % 2) * 18, 1 + (i / 2) * 18).setStandardSlotBackground().addIngredients(itemInputs.get(i));
        for (int i = 0; i < recipe.getFluidIngredients().size(); i++) {
            FluidStack fluid = recipe.getFluidIngredients().get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, 23 + (i % 2) * 18, 21 + (i / 2) * 18)
                    .setStandardSlotBackground().addFluidStack(fluid.getFluid(), fluid.getAmount()).setFluidRenderer(fluid.getAmount(), false, 16, 16);
        }
        for (int i = 0; i < itemOutputs.size(); i++)
            builder.addSlot(RecipeIngredientRole.OUTPUT, 96 + (i % 3) * 18, 1 + (i / 3) * 18).setStandardSlotBackground().addIngredients(itemOutputs.get(i));
        for (int i = 0; i < recipe.getFluidOutputs().size(); i++) {
            FluidStack fluid = recipe.getFluidOutputs().get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 96 + (i % 3) * 18, 21 + (i / 3) * 18)
                    .setStandardSlotBackground().addFluidStack(fluid.getFluid(), fluid.getAmount()).setFluidRenderer(fluid.getAmount(), false, 16, 16);
        }
    }

    @Override
    public void draw(GreenhouseRecipe recipe, @NotNull IRecipeSlotsView slots, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 66, 12);
        Minecraft minecraft = Minecraft.getInstance();
        graphics.drawString(minecraft.font, "Energy: " + recipe.getTotalEnergy() + " FE", 0, 42, 4210752, false);
        JeiOutputChanceHelper.drawItemChances(graphics, recipe.getItemOutputs(), 96, 1, 3, 18, 18);
    }
}
