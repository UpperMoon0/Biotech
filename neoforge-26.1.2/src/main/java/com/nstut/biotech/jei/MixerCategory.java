package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.MixerRecipe;
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

public class MixerCategory implements IRecipeCategory<MixerRecipe> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, MachineRegistries.MIXER.id());
    private static final int WIDTH = 150;
    private static final int HEIGHT = 69;
    public static final RecipeType<MixerRecipe> TYPE = new RecipeType<>(UID, MixerRecipe.class);

    private final IDrawable arrow;
    private final IDrawable icon;

    public MixerCategory(IGuiHelper helper) {
        arrow = helper.getRecipeArrow();
        icon = helper.createDrawableItemLike(MachineRegistries.MIXER.blockItem().get());
    }

    @Override public @NotNull RecipeType<MixerRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.MIXER.id()); }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull MixerRecipe recipe, @NotNull IFocusGroup focuses) {
        List<ItemStack> ingredients = recipe.getItemIngredients().stream().map(v -> v.getItemStack()).toList();
        List<ItemStack> itemOutputs = recipe.getItemOutputs().stream().map(v -> v.getItemStack()).toList();

        for (int i = 0; i < ingredients.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 23 + (i % 3) * 18, 1 + (i / 3) * 18)
                    .setStandardSlotBackground().addItemStack(ingredients.get(i));
        }
        for (int i = 0; i < recipe.getFluidIngredients().size(); i++) {
            FluidStack fluidStack = recipe.getFluidIngredients().get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, 23 + 18 * i, 38)
                    .setStandardSlotBackground().addFluidStack(fluidStack.getFluid(), fluidStack.getAmount())
                    .setFluidRenderer(fluidStack.getAmount(), false, 16, 16);
        }
        for (int i = 0; i < itemOutputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 9 + i * 18).setStandardSlotBackground().addItemStack(itemOutputs.get(i));
        }
        for (int i = 0; i < recipe.getFluidOutputs().size(); i++) {
            FluidStack fluidStack = recipe.getFluidOutputs().get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 9 + i * 18)
                    .setStandardSlotBackground().addFluidStack(fluidStack.getFluid(), fluidStack.getAmount())
                    .setFluidRenderer(fluidStack.getAmount(), false, 16, 16);
        }
    }

    @Override
    public void draw(MixerRecipe recipe, @NotNull IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 82, 14);
        graphics.text(Minecraft.getInstance().font, "Energy: " + recipe.getTotalEnergy() + " FE", 0, 59, 4210752, false);
        JeiOutputChanceHelper.drawItemChances(graphics, recipe.getItemOutputs(), 130, 9, 1, 18, 18);
    }
}
