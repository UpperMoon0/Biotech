package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.FermenterRecipe;
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

public class FermenterCategory implements IRecipeCategory<FermenterRecipe> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, MachineRegistries.FERMENTER.id());
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/jei/" + MachineRegistries.FERMENTER.id() + ".png");
    public static final RecipeType<FermenterRecipe> TYPE = new RecipeType<>(UID, FermenterRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public FermenterCategory(IGuiHelper helper) {
        background = helper.createDrawable(TEXTURE, 0, 0, 148, 52);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MachineRegistries.FERMENTER.blockItem().get()));
    }

    @Override public @NotNull RecipeType<FermenterRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.FERMENTER.id()); }
    @Override public int getWidth() { return background.getWidth(); }
    @Override public int getHeight() { return background.getHeight(); }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull FermenterRecipe recipe, @NotNull IFocusGroup focuses) {
        List<ItemStack> ingredients = recipe.getItemIngredients().stream().map(v -> v.getItemStack()).toList();
        List<ItemStack> itemOutputs = recipe.getItemOutputs().stream().map(v -> v.getItemStack()).toList();

        for (int i = 0; i < ingredients.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 23 + (i % 3) * 18, 2 + (i / 3) * 18)
                    .addItemStack(ingredients.get(i));
        }
        for (int i = 0; i < recipe.getFluidIngredients().size(); i++) {
            FluidStack fluidStack = recipe.getFluidIngredients().get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, 79, 2 + i * 18)
                    .addFluidStack(fluidStack.getFluid(), fluidStack.getAmount())
                    .setFluidRenderer(fluidStack.getAmount(), false, 16, 16);
        }
        for (int i = 0; i < itemOutputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 2 + i * 18).addItemStack(itemOutputs.get(i));
        }
        for (int i = 0; i < recipe.getFluidOutputs().size(); i++) {
            FluidStack fluidStack = recipe.getFluidOutputs().get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 131, 2 + i * 18)
                    .addFluidStack(fluidStack.getFluid(), fluidStack.getAmount())
                    .setFluidRenderer(fluidStack.getAmount(), false, 16, 16);
        }
    }

    @Override
    public void draw(FermenterRecipe recipe, @NotNull IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        background.draw(graphics);
        graphics.text(Minecraft.getInstance().font, "Energy: " + recipe.getTotalEnergy() + " FE", 0, 42, 4210752, false);
    }
}
