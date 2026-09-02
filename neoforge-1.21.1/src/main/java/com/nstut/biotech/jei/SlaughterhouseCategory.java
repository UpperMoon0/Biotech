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
        this.background = helper.createDrawable(TEXTURE, 0, 0, 136, 70);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MachineRegistries.SLAUGHTERHOUSE.blockItem().get()));
    }

    @Override
    public @NotNull RecipeType<SlaughterhouseRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.biotech." + MachineRegistries.SLAUGHTERHOUSE.id());
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SlaughterhouseRecipe recipe, @NotNull IFocusGroup focuses) {

        Ingredient itemIngredient = Ingredient.of(recipe.getItemIngredients().get(0).getItemStack());
        FluidStack fluidIngredient = recipe.getFluidIngredients().get(0);
        List<Ingredient> itemOutputs = recipe.getItemOutputs().stream()
                .map(outputItem -> Ingredient.of(outputItem.getItemStack()))
                .toList();

        builder.addSlot(RecipeIngredientRole.INPUT, 23, 4).addIngredients(itemIngredient);
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 24).addFluidStack(fluidIngredient.getFluid(), fluidIngredient.getAmount()).setFluidRenderer(fluidIngredient.getAmount(), false, 16, 16);

        for (int i = 0; i < itemOutputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 79 + (i % 3) * 20, 1 + (i / 3) * 28).addIngredients(itemOutputs.get(i));
        }
    }


    @Override
    public void draw(SlaughterhouseRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        int energy = recipe.getTotalEnergy();
        guiGraphics.drawString(minecraft.font, "Energy: " + energy + " FE", 0, 60, 4210752, false);
        List<OutputItem> outputItems = recipe.getItemOutputs();
        for (int i = 0; i < outputItems.size(); i++) {
            String chance = "";
            if (recipe.getItemOutputs().get(i).getChance() < 1) {
                chance = (int) (recipe.getItemOutputs().get(i).getChance() * 100) + "%";
            }
            guiGraphics.drawString(minecraft.font, chance, 79 + (i % 3) * 20,20 + (i / 3) * 28, 4210752, false);
        }
    }
}
