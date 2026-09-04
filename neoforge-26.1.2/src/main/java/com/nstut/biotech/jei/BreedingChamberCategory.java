package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.BreedingChamberRecipe;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class BreedingChamberCategory implements IRecipeCategory<BreedingChamberRecipe> {
    public static final Identifier UID=Identifier.fromNamespaceAndPath(Biotech.MOD_ID,MachineRegistries.BREEDING_CHAMBER.id());
    public static final Identifier TEXTURE=Identifier.fromNamespaceAndPath(Biotech.MOD_ID,"textures/gui/jei/"+MachineRegistries.BREEDING_CHAMBER.id()+".png");
    public static final RecipeType<BreedingChamberRecipe> TYPE=new RecipeType<>(UID,BreedingChamberRecipe.class);
    private final IDrawable background; private final IDrawable icon;
    public BreedingChamberCategory(IGuiHelper helper){background=helper.createDrawable(TEXTURE,0,0,122,52);icon=helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(MachineRegistries.BREEDING_CHAMBER.blockItem().get()));}
    @Override public @NotNull RecipeType<BreedingChamberRecipe> getRecipeType(){return TYPE;} @Override public @NotNull Component getTitle(){return Component.translatable("block.biotech."+MachineRegistries.BREEDING_CHAMBER.id());} @Override public int getWidth(){return background.getWidth();} @Override public int getHeight(){return background.getHeight();} @Override public @NotNull IDrawable getIcon(){return icon;}
    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder,@NotNull BreedingChamberRecipe recipe,@NotNull IFocusGroup focuses){List<ItemStack> items=recipe.getItemIngredients().stream().map(v->v.getItemStack()).toList();FluidStack fluid=recipe.getFluidIngredients().get(0);List<ItemStack> outputs=recipe.getItemOutputs().stream().map(v->v.getItemStack()).toList();builder.addSlot(RecipeIngredientRole.INPUT,23,1).addItemStack(items.get(0));builder.addSlot(RecipeIngredientRole.INPUT,41,1).addItemStack(items.get(1));builder.addSlot(RecipeIngredientRole.INPUT,32,21).addFluidStack(fluid.getFluid(),fluid.getAmount()).setFluidRenderer(fluid.getAmount(),false,16,16);builder.addSlot(RecipeIngredientRole.OUTPUT,101,11).addItemStack(outputs.get(0));}
    @Override public void draw(BreedingChamberRecipe recipe,@NotNull IRecipeSlotsView slots,GuiGraphicsExtractor g,double mouseX,double mouseY){background.draw(g);g.text(Minecraft.getInstance().font,"Energy: "+recipe.getTotalEnergy()+" FE",0,42,4210752,false);}
}
