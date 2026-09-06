package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.GreenhouseRecipe;
import com.nstut.nstutlib.recipes.OutputItem;
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

public class GreenhouseCategory implements IRecipeCategory<GreenhouseRecipe> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, MachineRegistries.GREENHOUSE.id());
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/jei/" + MachineRegistries.GREENHOUSE.id() + ".png");
    public static final RecipeType<GreenhouseRecipe> TYPE = new RecipeType<>(UID, GreenhouseRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public GreenhouseCategory(IGuiHelper helper) {
        background = helper.createDrawable(TEXTURE, 0, 0, 153, 52);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MachineRegistries.GREENHOUSE.blockItem().get()));
    }

    @Override public @NotNull RecipeType<GreenhouseRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.GREENHOUSE.id()); }
    @Override public int getWidth() { return background.getWidth(); }
    @Override public int getHeight() { return background.getHeight(); }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull GreenhouseRecipe recipe, @NotNull IFocusGroup focuses) {
        List<ItemStack> ingredients = recipe.getItemIngredients().stream().map(v -> v.getItemStack()).toList();
        FluidStack fluid = recipe.getFluidIngredients().get(0);
        List<ItemStack> outputs = recipe.getItemOutputs().stream().map(v -> v.getItemStack()).toList();

        builder.addSlot(RecipeIngredientRole.INPUT, 23, 1).addItemStack(ingredients.get(0));
        if (ingredients.size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, 41, 1).addItemStack(ingredients.get(1));
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 32, 21)
                .addFluidStack(fluid.getFluid(), fluid.getAmount())
                .setFluidRenderer(fluid.getAmount(), false, 16, 16);
        for (int i = 0; i < outputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 96 + i * 20, 6).addItemStack(outputs.get(i));
        }
    }

    @Override
    public void draw(GreenhouseRecipe recipe, @NotNull IRecipeSlotsView slots, @NotNull GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        background.draw(graphics);
        Minecraft minecraft = Minecraft.getInstance();
        graphics.text(minecraft.font, "Energy: " + recipe.getTotalEnergy() + " FE", 0, 42, 4210752, false);

        List<OutputItem> outputs = recipe.getItemOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            String chance = outputs.get(i).getChance() < 1 ? (int) (outputs.get(i).getChance() * 100) + "%" : "";
            graphics.text(minecraft.font, chance, 96 + i * 20, 25, 4210752, false);
        }
    }
}
