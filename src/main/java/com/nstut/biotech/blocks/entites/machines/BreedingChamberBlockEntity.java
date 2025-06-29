package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.BreedingChamberPacket;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.recipes.BreedingChamberRecipe;
import com.nstut.biotech.views.machines.menu.BreedingChamberMenu;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.blocks.IMachineHatch;
import com.nstut.nstutlib.machines.config.MachineConfig;
import com.nstut.nstutlib.recipes.ModRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class BreedingChamberBlockEntity extends MachineBlockEntity {


    public BreedingChamberBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistries.BREEDING_CHAMBER.blockEntity().get(), pos, state);
    }
    @Override
    public AbstractContainerMenu createMenu(int pContainerId,
                                            @NotNull Inventory pPlayerInventory,
                                            @NotNull Player pPlayer) {
        return new BreedingChamberMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        IItemHandler combinedInputItemHandler = new CombinedInvWrapper(
                (IItemHandlerModifiable) ((ItemInputHatchBlockEntity) getHatch("item_input", 0)).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new),
                (IItemHandlerModifiable) ((ItemInputHatchBlockEntity) getHatch("item_input", 1)).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new),
                (IItemHandlerModifiable) ((ItemInputHatchBlockEntity) getHatch("item_input", 2)).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new)
        );
        IItemHandler outputItemHandler = ((ItemOutputHatchBlockEntity) getHatch("item_output", 0)).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
        IFluidHandler inputFluidHandler = ((FluidInputHatchBlockEntity) getHatch("fluid_input", 0)).getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(NullPointerException::new);
        IEnergyStorage energyStorage = ((EnergyInputHatchBlockEntity) getHatch("energy_input", 0)).getCapability(ForgeCapabilities.ENERGY).orElseThrow(NullPointerException::new);

        int energyCapacity = energyInputHatch.ENERGY_CAPACITY;
        int energyStored = energyStorage.getEnergyStored();
        int energyConsumeRate = energyInputHatch.ENERGY_THROUGHPUT;
        int fluidCapacity = FluidInputHatchBlockEntity.TANK_CAPACITY;
        FluidStack fluidStored = inputFluidHandler.getFluidInTank(0);

        if (recipeHandler.isEmpty()) {
            energyConsumed = 0;
            recipeHandler = level
                    .getRecipeManager()
                    .getAllRecipesFor(BreedingChamberRecipe.TYPE)
                    .stream()
                    .filter(r -> r.recipeMatch(
                            combinedInputItemHandler,
                            List.of(inputFluidHandler),
                            outputItemHandler,
                            null))
                    .findFirst();
        } else {
            BreedingChamberRecipe recipeHandler = (BreedingChamberRecipe) this.recipeHandler.get();
            recipeEnergyCost = recipeHandler.getTotalEnergy();

            if (energyConsumed == 0) {
                recipeHandler.consumeIngredients(combinedInputItemHandler, List.of(inputFluidHandler));
            }

            if (energyStorage.getEnergyStored() >= energyConsumeRate) {
                int energyToConsume = Math.min(energyConsumeRate, recipeEnergyCost - energyConsumed);
                energyConsumed += energyToConsume;
                energyStorage.extractEnergy(energyToConsume, false);
            }

            if (energyConsumed == recipeEnergyCost) {
                energyConsumed = 0;
                recipeHandler.assemble(outputItemHandler, null);

                this.recipeHandler = level
                        .getRecipeManager()
                        .getAllRecipesFor(BreedingChamberRecipe.TYPE)
                        .stream()
                        .filter(r -> r.recipeMatch(
                                combinedInputItemHandler,
                                List.of(inputFluidHandler),
                                outputItemHandler,
                                null))
                        .findFirst();            }
        }

        PacketRegistries.sendToClients(new BreedingChamberPacket(
                energyCapacity,
                energyStored,
                energyConsumeRate,
                energyConsumed,
                recipeEnergyCost,
                fluidCapacity,
                fluidStored,
                isStructureValid,
                blockPos,
                recipeHandler.map(ModRecipe::getRecipe).orElse(null)
        ));
    }


    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biotech.breeding_chamber");
    }
}