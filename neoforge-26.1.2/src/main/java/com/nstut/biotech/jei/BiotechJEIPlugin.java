package com.nstut.biotech.jei;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.client.ClientRecipeSync;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class BiotechJEIPlugin implements IModPlugin {
    @Override public void registerItemSubtypes(mezz.jei.api.registration.ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(com.nstut.biotech.items.ItemRegistries.CAPTURED_ANIMAL.get(), (stack, context) -> com.nstut.biotech.items.CapturedAnimalItem.recipeSubtype(stack));
    }
    private java.util.List<GreenhouseRecipe> harvestRecipes = java.util.List.of();
    private java.util.List<SlaughterhouseRecipe> lootRecipes = java.util.List.of();
    @Override public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime runtime) {
        SlaughterhouseLootPreview.onRefresh(() -> {
            runtime.getRecipeManager().hideRecipes(SlaughterhouseCategory.TYPE, lootRecipes);
            lootRecipes = lootRecipes.stream().map(recipe -> new SlaughterhouseRecipe(recipe.getId(), recipe.getRecipe().copy())).toList();
            runtime.getRecipeManager().addRecipes(SlaughterhouseCategory.TYPE, lootRecipes);
            runtime.getRecipeManager().hideRecipes(GreenhouseCategory.TYPE, harvestRecipes);
            harvestRecipes = harvestRecipes.stream().map(recipe -> new GreenhouseRecipe(recipe.getId(), recipe.getRecipe().copy())).toList();
            runtime.getRecipeManager().addRecipes(GreenhouseCategory.TYPE, harvestRecipes);
        });
    }
    @Override public void onRuntimeUnavailable() { SlaughterhouseLootPreview.onRefresh(() -> {}); }
    @Override public @NotNull Identifier getPluginUid() { return Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "jei_plugin"); }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new BreedingChamberCategory(gui));
        registration.addRecipeCategories(new TerrestrialHabitatCategory(gui));
        registration.addRecipeCategories(new SlaughterhouseCategory(gui));
        registration.addRecipeCategories(new GreenhouseCategory(gui));
        registration.addRecipeCategories(new FermenterCategory(gui));
        registration.addRecipeCategories(new MixerCategory(gui));
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
        var recipes = ClientRecipeSync.get();
        registration.addRecipes(BreedingChamberCategory.TYPE, values(recipes.byType(BreedingChamberRecipe.TYPE)));
        registration.addRecipes(TerrestrialHabitatCategory.TYPE, values(recipes.byType(TerrestrialHabitatRecipe.TYPE)));
        lootRecipes = recipes.byType(SlaughterhouseRecipe.TYPE).stream()
                .map(holder -> new SlaughterhouseRecipe(holder.id().identifier(), holder.value().getRecipe().copy())).toList();
        registration.addRecipes(SlaughterhouseCategory.TYPE, lootRecipes);
        harvestRecipes = recipes.byType(GreenhouseRecipe.TYPE).stream()
                .map(holder -> new GreenhouseRecipe(holder.id().identifier(), holder.value().getRecipe().copy())).toList();
        registration.addRecipes(GreenhouseCategory.TYPE, harvestRecipes);
        registration.addRecipes(FermenterCategory.TYPE, values(recipes.byType(FermenterRecipe.TYPE)));
        registration.addRecipes(MixerCategory.TYPE, values(recipes.byType(MixerRecipe.TYPE)));
    }

    private static <T extends net.minecraft.world.item.crafting.RecipeInput, R extends net.minecraft.world.item.crafting.Recipe<T>> java.util.List<R> values(java.util.Collection<RecipeHolder<R>> holders) {
        return holders.stream().map(RecipeHolder::value).toList();
    }
}
