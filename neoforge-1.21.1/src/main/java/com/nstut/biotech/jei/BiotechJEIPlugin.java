package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
@JeiPlugin
public class BiotechJEIPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new BreedingChamberCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new TerrestrialHabitatCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SlaughterhouseCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new GreenhouseCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FermenterCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MixerCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(MachineRegistries.BREEDING_CHAMBER.blockItem().get()), BreedingChamberCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(MachineRegistries.TERRESTRIAL_HABITAT.blockItem().get()), TerrestrialHabitatCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(MachineRegistries.SLAUGHTERHOUSE.blockItem().get()), SlaughterhouseCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(MachineRegistries.GREENHOUSE.blockItem().get()), GreenhouseCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(MachineRegistries.FERMENTER.blockItem().get()), FermenterCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(MachineRegistries.MIXER.blockItem().get()), MixerCategory.TYPE);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        RecipeManager recipeManager = level.getRecipeManager();

        List<BreedingChamberRecipe> breedingRecipes = recipeManager.getAllRecipesFor(BreedingChamberRecipe.TYPE).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(BreedingChamberCategory.TYPE, breedingRecipes);

        List<TerrestrialHabitatRecipe> terrestrialRecipes = recipeManager.getAllRecipesFor(TerrestrialHabitatRecipe.TYPE).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(TerrestrialHabitatCategory.TYPE, terrestrialRecipes);

        List<SlaughterhouseRecipe> slaughterhouseRecipes = recipeManager.getAllRecipesFor(SlaughterhouseRecipe.TYPE).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(SlaughterhouseCategory.TYPE, slaughterhouseRecipes);

        List<GreenhouseRecipe> greenhouseRecipes = recipeManager.getAllRecipesFor(GreenhouseRecipe.TYPE).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(GreenhouseCategory.TYPE, greenhouseRecipes);

        List<FermenterRecipe> fermenterRecipes = recipeManager.getAllRecipesFor(FermenterRecipe.TYPE).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(FermenterCategory.TYPE, fermenterRecipes);

        List<MixerRecipe> mixerRecipes = recipeManager.getAllRecipesFor(MixerRecipe.TYPE).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(MixerCategory.TYPE, mixerRecipes);
    }
}
