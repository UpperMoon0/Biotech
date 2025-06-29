package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.network.TerrestrialHabitatPacket;
import com.nstut.biotech.recipes.TerrestrialHabitatRecipe;
import com.nstut.biotech.views.machines.menu.TerrestrialHabitatMenu;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.recipes.ModRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
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

public class TerrestrialHabitatBlockEntity extends MachineBlockEntity {


    public TerrestrialHabitatBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(MachineRegistries.TERRESTRIAL_HABITAT.blockEntity().get(), pPos, pBlockState);
    }

    @Override
    public AbstractContainerMenu createMenu(int pContainerId,
                                            @NotNull Inventory pPlayerInventory,
                                            @NotNull Player pPlayer) {
        return new TerrestrialHabitatMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        if (!areHatchesSet()) { super.setHatches(blockPos, level); }
        IItemHandler combinedInputItemHandler = new CombinedInvWrapper(
                (IItemHandlerModifiable) getItemInputHatches().get(0).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new),
                (IItemHandlerModifiable) getItemInputHatches().get(1).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new),
                (IItemHandlerModifiable) getItemInputHatches().get(2).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new)
        );
        IItemHandler outputItemHandler = getItemOutputHatches().get(0).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
        IFluidHandler inputFluidHandler = getFluidInputHatches().get(0).getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(NullPointerException::new);
        IEnergyStorage energyStorage = getEnergyInputHatches().get(0).getCapability(ForgeCapabilities.ENERGY).orElseThrow(NullPointerException::new);

        int energyCapacity = getEnergyInputHatches().get(0).ENERGY_CAPACITY;
        int energyStored = energyStorage.getEnergyStored();
        int energyConsumeRate = getEnergyInputHatches().get(0).ENERGY_THROUGHPUT;
        int fluidCapacity = FluidInputHatchBlockEntity.TANK_CAPACITY;
        FluidStack fluidStored = inputFluidHandler.getFluidInTank(0);

        if (recipeHandler.isEmpty()) {
            energyConsumed = 0;
            recipeHandler = level
                    .getRecipeManager()
                    .getAllRecipesFor(TerrestrialHabitatRecipe.TYPE)
                    .stream()
                    .filter(r -> r.recipeMatch(
                            combinedInputItemHandler,
                            List.of(inputFluidHandler),
                            outputItemHandler,
                            null))
                    .findFirst();
        } else {
            TerrestrialHabitatRecipe recipeHandler = (TerrestrialHabitatRecipe) this.recipeHandler.get();
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
                        .getAllRecipesFor(TerrestrialHabitatRecipe.TYPE)
                        .stream().filter(r -> r.recipeMatch(
                                combinedInputItemHandler,
                                List.of(inputFluidHandler),
                                outputItemHandler,
                                null))
                        .findFirst();            }
        }

        PacketRegistries.sendToClients(new TerrestrialHabitatPacket(
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
        return Component.translatable("block.biotech.terrestrial_habitat");
    }
}