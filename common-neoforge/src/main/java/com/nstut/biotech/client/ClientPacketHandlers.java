package com.nstut.biotech.client;

import com.nstut.biotech.blocks.entites.hatches.EnergyHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidHatchBlockEntity;
import com.nstut.biotech.blocks.entites.machines.BreedingChamberBlockEntity;
import com.nstut.biotech.blocks.entites.machines.FermenterBlockEntity;
import com.nstut.biotech.blocks.entites.machines.GreenhouseBlockEntity;
import com.nstut.biotech.blocks.entites.machines.MixerBlockEntity;
import com.nstut.biotech.blocks.entites.machines.SlaughterhouseBlockEntity;
import com.nstut.biotech.blocks.entites.machines.TerrestrialHabitatBlockEntity;
import com.nstut.biotech.views.io_hatches.energy.EnergyHatchMenu;
import com.nstut.biotech.views.io_hatches.fluid.FluidHatchMenu;
import com.nstut.biotech.views.machines.menu.BreedingChamberMenu;
import com.nstut.biotech.views.machines.menu.FermenterMenu;
import com.nstut.biotech.views.machines.menu.GreenhouseMenu;
import com.nstut.biotech.views.machines.menu.MixerMenu;
import com.nstut.biotech.views.machines.menu.SlaughterhouseMenu;
import com.nstut.biotech.views.machines.menu.TerrestrialHabitatMenu;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void handleFluidHatch(FluidStack fluidStack, BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null) return;
        if (minecraft.level.getBlockEntity(pos) instanceof FluidHatchBlockEntity blockEntity) {
            blockEntity.setFluid(fluidStack);
            if (player.containerMenu instanceof FluidHatchMenu menu
                    && menu.getFluidHatchBlockEntity().getBlockPos().equals(pos)) {
                menu.setFluidStack(fluidStack.copy());
            }
        }
    }

    public static void handleEnergy(int energy, BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null) return;
        if (minecraft.level.getBlockEntity(pos) instanceof EnergyHatchBlockEntity blockEntity) {
            blockEntity.setEnergy(energy);
            if (player.containerMenu instanceof EnergyHatchMenu menu
                    && menu.getBlockEntity().getBlockPos().equals(pos)) {
                menu.setEnergy(energy);
            }
        }
    }

    public static void handleBreedingChamber(int energyCapacity, int energyStored, int energyConsumeRate,
                                             int consumedEnergy, int recipeEnergyCost, int fluidCapacity,
                                             FluidStack fluidStored, boolean structureValid, BlockPos pos,
                                             ModRecipeData recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null
                || !(minecraft.level.getBlockEntity(pos) instanceof BreedingChamberBlockEntity)) return;
        if (player.containerMenu instanceof BreedingChamberMenu menu && menu.getBlockEntity().getBlockPos().equals(pos)) {
            menu.setEnergyCapacity(energyCapacity);
            menu.setEnergyStored(energyStored);
            menu.setEnergyConsumeRate(energyConsumeRate);
            menu.setEnergyConsumed(consumedEnergy);
            menu.setRecipeEnergyCost(recipeEnergyCost);
            menu.setFluidCapacity(fluidCapacity);
            menu.setFluidStored(fluidStored.copy());
            menu.setStructureValid(structureValid);
            menu.setRecipe(recipe);
        }
    }

    public static void handleTerrestrialHabitat(int energyCapacity, int energyStored, int energyConsumeRate,
                                                int consumedEnergy, int recipeEnergyCost, int fluidCapacity,
                                                FluidStack fluidStored, boolean structureValid, BlockPos pos,
                                                ModRecipeData recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null
                || !(minecraft.level.getBlockEntity(pos) instanceof TerrestrialHabitatBlockEntity)) return;
        if (player.containerMenu instanceof TerrestrialHabitatMenu menu && menu.getBlockEntity().getBlockPos().equals(pos)) {
            menu.setEnergyCapacity(energyCapacity);
            menu.setEnergyStored(energyStored);
            menu.setEnergyConsumeRate(energyConsumeRate);
            menu.setEnergyConsumed(consumedEnergy);
            menu.setRecipeEnergyCost(recipeEnergyCost);
            menu.setFluidCapacity(fluidCapacity);
            menu.setFluidStored(fluidStored.copy());
            menu.setStructureValid(structureValid);
            menu.setRecipe(recipe);
        }
    }

    public static void handleSlaughterhouse(int energyCapacity, int energyStored, int energyConsumeRate,
                                            int consumedEnergy, int recipeEnergyCost, int fluidCapacity,
                                            FluidStack fluidStored, boolean structureValid, BlockPos pos,
                                            ModRecipeData recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null
                || !(minecraft.level.getBlockEntity(pos) instanceof SlaughterhouseBlockEntity)) return;
        if (player.containerMenu instanceof SlaughterhouseMenu menu && menu.getBlockEntity().getBlockPos().equals(pos)) {
            menu.setEnergyCapacity(energyCapacity);
            menu.setEnergyStored(energyStored);
            menu.setEnergyConsumeRate(energyConsumeRate);
            menu.setEnergyConsumed(consumedEnergy);
            menu.setRecipeEnergyCost(recipeEnergyCost);
            menu.setFluidCapacity(fluidCapacity);
            menu.setFluidStored(fluidStored.copy());
            menu.setStructureValid(structureValid);
            menu.setRecipe(recipe);
        }
    }

    public static void handleGreenhouse(int energyCapacity, int energyStored, int energyConsumeRate,
                                        int consumedEnergy, int recipeEnergyCost, int fluidCapacity,
                                        FluidStack fluidStored, boolean structureValid, BlockPos pos,
                                        ModRecipeData recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null
                || !(minecraft.level.getBlockEntity(pos) instanceof GreenhouseBlockEntity)) return;
        if (player.containerMenu instanceof GreenhouseMenu menu && menu.getBlockEntity().getBlockPos().equals(pos)) {
            menu.setEnergyCapacity(energyCapacity);
            menu.setEnergyStored(energyStored);
            menu.setEnergyConsumeRate(energyConsumeRate);
            menu.setEnergyConsumed(consumedEnergy);
            menu.setRecipeEnergyCost(recipeEnergyCost);
            menu.setFluidCapacity(fluidCapacity);
            menu.setFluidStored(fluidStored.copy());
            menu.setStructureValid(structureValid);
            menu.setRecipe(recipe);
        }
    }

    public static void handleFermenter(int energyCapacity, int energyStored, int energyConsumeRate,
                                       int consumedEnergy, int recipeEnergyCost, int fluidCapacity,
                                       FluidStack fluidStored, boolean structureValid, BlockPos pos,
                                       ModRecipeData recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null
                || !(minecraft.level.getBlockEntity(pos) instanceof FermenterBlockEntity)) return;
        if (player.containerMenu instanceof FermenterMenu menu && menu.getBlockEntity().getBlockPos().equals(pos)) {
            menu.setEnergyCapacity(energyCapacity);
            menu.setEnergyStored(energyStored);
            menu.setEnergyConsumeRate(energyConsumeRate);
            menu.setEnergyConsumed(consumedEnergy);
            menu.setRecipeEnergyCost(recipeEnergyCost);
            menu.setFluidCapacity(fluidCapacity);
            menu.setFluidStored(fluidStored.copy());
            menu.setStructureValid(structureValid);
            menu.setRecipe(recipe);
        }
    }

    public static void handleMixer(int energyCapacity, int energyStored, int energyConsumeRate,
                                   int consumedEnergy, int recipeEnergyCost, boolean structureValid,
                                   BlockPos pos, ModRecipeData recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null
                || !(minecraft.level.getBlockEntity(pos) instanceof MixerBlockEntity)) return;
        if (player.containerMenu instanceof MixerMenu menu && menu.getBlockEntity().getBlockPos().equals(pos)) {
            menu.setEnergyCapacity(energyCapacity);
            menu.setEnergyStored(energyStored);
            menu.setEnergyConsumeRate(energyConsumeRate);
            menu.setEnergyConsumed(consumedEnergy);
            menu.setRecipeEnergyCost(recipeEnergyCost);
            menu.setStructureValid(structureValid);
            menu.setRecipe(recipe);
        }
    }
}
