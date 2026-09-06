package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.SlaughterhouseRecipe;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SlaughterhouseCategory implements IRecipeCategory<SlaughterhouseRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Biotech.MOD_ID, MachineRegistries.SLAUGHTERHOUSE.id());
    public static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/jei/" + MachineRegistries.SLAUGHTERHOUSE.id() + ".png");
    public static final RecipeType<SlaughterhouseRecipe> TYPE = new RecipeType<>(UID, SlaughterhouseRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;

    public SlaughterhouseCategory(IGuiHelper helper) {
        background = helper.createDrawable(TEXTURE, 0, 0, 136, 70);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MachineRegistries.SLAUGHTERHOUSE.blockItem().get()));
    }

    @Override public @NotNull RecipeType<SlaughterhouseRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("block.biotech." + MachineRegistries.SLAUGHTERHOUSE.id()); }
    @Override public @NotNull IDrawable getBackground() { return background; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SlaughterhouseRecipe recipe, @NotNull IFocusGroup focuses) {
        List<Ingredient> itemInputs = recipe.getItemIngredients().stream().map(v -> Ingredient.of(v.getItemStack())).toList();
        List<Ingredient> itemOutputs = recipe.getItemOutputs().stream().map(v -> Ingredient.of(v.getItemStack())).toList();
        for (int i = 0; i < itemInputs.size(); i++)
            builder.addSlot(RecipeIngredientRole.INPUT, 23 + (i % 2) * 18, 4 + (i / 2) * 18).addIngredients(itemInputs.get(i));
        for (int i = 0; i < recipe.getFluidIngredients().size(); i++) {
            FluidStack fluid = recipe.getFluidIngredients().get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, 23 + (i % 2) * 18, 24 + (i / 2) * 18)
                    .addFluidStack(fluid.getFluid(), fluid.getAmount()).setFluidRenderer(fluid.getAmount(), false, 16, 16);
        }
        for (int i = 0; i < itemOutputs.size(); i++)
            builder.addSlot(RecipeIngredientRole.OUTPUT, 79 + (i % 3) * 18, 1 + (i / 3) * 18).addIngredients(itemOutputs.get(i));
        for (int i = 0; i < recipe.getFluidOutputs().size(); i++) {
            FluidStack fluid = recipe.getFluidOutputs().get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 79 + (i % 3) * 18, 37 + (i / 3) * 18)
                    .addFluidStack(fluid.getFluid(), fluid.getAmount()).setFluidRenderer(fluid.getAmount(), false, 16, 16);
        }
    }

    @Override
    public void draw(SlaughterhouseRecipe recipe, @NotNull IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.drawString(minecraft.font, "Energy: " + recipe.getTotalEnergy() + " FE", 0, 60, 4210752, false);
        List<OutputItem> outputs = recipe.getItemOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            String chance = outputs.get(i).getChance() < 1 ? (int) (outputs.get(i).getChance() * 100) + "%" : "";
            graphics.drawString(minecraft.font, chance, 79 + (i % 3) * 18, 20 + (i / 3) * 18, 4210752, false);
        }
    }
}
