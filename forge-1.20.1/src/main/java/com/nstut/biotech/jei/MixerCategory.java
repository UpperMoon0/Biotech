package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.MixerRecipe;
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

public class MixerCategory implements IRecipeCategory<MixerRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(Biotech.MOD_ID, MachineRegistries.MIXER.id());
    public static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/jei/" + MachineRegistries.MIXER.id() + ".png");

    public static final RecipeType<MixerRecipe> TYPE = new RecipeType<>(UID, MixerRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public MixerCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 129, 69);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MachineRegistries.MIXER.blockItem().get()));
    }

    @Override
    public @NotNull RecipeType<MixerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.biotech." + MachineRegistries.MIXER.id());
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull MixerRecipe recipe, @NotNull IFocusGroup focuses) {

        List<Ingredient> ingredients = recipe.getItemIngredients().stream()
                .map(ingredientItem -> Ingredient.of(ingredientItem.getItemStack()))
                .toList();
        List<Ingredient> itemOutputs = recipe.getItemOutputs().stream()
                .map(outputItem -> Ingredient.of(outputItem.getItemStack()))
                .toList();

        for (int i = 0; i < ingredients.size(); i++) {
            int x = 23 + (i % 3) * 18;
            int y = 1 + (i / 3) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredients(ingredients.get(i));
        }

        for (int i = 0; i < recipe.getFluidIngredients().size(); i++) {
            FluidStack fluidStack = recipe.getFluidIngredients().get(i);
            int x = 23 + 18 * i;
            int y = 38;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addFluidStack(fluidStack.getFluid(), fluidStack.getAmount()).setFluidRenderer(fluidStack.getAmount(), false, 16, 16);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 9).addIngredients(itemOutputs.get(0));
        if (!recipe.getFluidOutputs().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 27).addFluidStack(recipe.getFluidOutputs().get(0).getFluid(), recipe.getFluidOutputs().get(0).getAmount()).setFluidRenderer(recipe.getFluidOutputs().get(0).getAmount(), false, 16, 16);
        }
    }

    @Override
    public void draw(MixerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        int energy = recipe.getTotalEnergy();
        guiGraphics.drawString(minecraft.font, "Energy: " + energy + " FE", 0, 59, 4210752, false);
    }
}