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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FermenterCategory implements IRecipeCategory<FermenterRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(Biotech.MOD_ID, MachineRegistries.FERMENTER.id());
    public static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/jei/" + MachineRegistries.FERMENTER.id() + ".png");

    public static final RecipeType<FermenterRecipe> TYPE = new RecipeType<>(UID, FermenterRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public FermenterCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 148, 52);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MachineRegistries.FERMENTER.blockItem().get()));
    }

    @Override
    public @NotNull RecipeType<FermenterRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.biotech." + MachineRegistries.FERMENTER.id());
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull FermenterRecipe recipe, @NotNull IFocusGroup focuses) {

        List<Ingredient> ingredients = recipe.getItemIngredients().stream()
                .map(ingredientItem -> Ingredient.of(ingredientItem.getItemStack()))
                .toList();
        FluidStack fluidIngredient = recipe.getFluidIngredients().get(0);
        List<Ingredient> itemOutputs = recipe.getItemOutputs().stream()
                .map(outputItem -> Ingredient.of(outputItem.getItemStack()))
                .toList();

        for (int i = 0; i < ingredients.size(); i++) {
            int x = 23 + (i % 3) * 18;
            int y = 2 + (i / 3) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredients(ingredients.get(i));
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 79, 11).addFluidStack(fluidIngredient.getFluid(), fluidIngredient.getAmount()).setFluidRenderer(fluidIngredient.getAmount(), false, 16, 16);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 131, 11).addIngredients(itemOutputs.get(0));
    }

    @Override
    public void draw(FermenterRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        int energy = recipe.getTotalEnergy();
        guiGraphics.drawString(minecraft.font, "Energy: " + energy + " FE", 0, 42, 4210752, false);
    }
}