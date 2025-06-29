package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.*;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.MixerPacket;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.recipes.MixerRecipe;
import com.nstut.biotech.views.machines.menu.MixerMenu;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockPattern;
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

public class MixerBlockEntity extends MachineBlockEntity {

    private ItemInputHatchBlockEntity itemInputHatch;
    private ItemOutputHatchBlockEntity itemOutputHatch;
    private EnergyInputHatchBlockEntity energyInputHatch;
    private FluidInputHatchBlockEntity fluidInputHatch1;
    private FluidInputHatchBlockEntity fluidInputHatch2;
    private FluidInputHatchBlockEntity fluidInputHatch3;
    private FluidOutputHatchBlockEntity fluidOutputHatch;

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistries.MIXER.blockEntity().get(), pos, state);
    }
    @Override
    public AbstractContainerMenu createMenu(int pContainerId,
                                            @NotNull Inventory pPlayerInventory,
                                            @NotNull Player pPlayer) {
        return new MixerMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        IItemHandler combinedInputItemHandler = new CombinedInvWrapper(
                (IItemHandlerModifiable) itemInputHatch.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new)
        );
        IItemHandler outputItemHandler = itemOutputHatch.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
        IFluidHandler inputFluidHandler1 = fluidInputHatch1.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(NullPointerException::new);
        IFluidHandler inputFluidHandler2 = fluidInputHatch2.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(NullPointerException::new);
        IFluidHandler inputFluidHandler3 = fluidInputHatch3.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(NullPointerException::new);
        IFluidHandler outputFluidHandler = fluidOutputHatch.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(NullPointerException::new);
        IEnergyStorage energyStorage = energyInputHatch.getCapability(ForgeCapabilities.ENERGY).orElseThrow(NullPointerException::new);

        int energyCapacity = energyInputHatch.ENERGY_CAPACITY;
        int energyStored = energyStorage.getEnergyStored();
        int energyConsumeRate = energyInputHatch.ENERGY_THROUGHPUT;
        int fluidCapacity = FluidInputHatchBlockEntity.TANK_CAPACITY;
        FluidStack fluidStored = inputFluidHandler1.getFluidInTank(0);

        if (recipeHandler.isEmpty()) {
            energyConsumed = 0;
            recipeHandler = level
                    .getRecipeManager()
                    .getAllRecipesFor(MixerRecipe.TYPE)
                    .stream()
                    .filter(r -> r.recipeMatch(
                            combinedInputItemHandler,
                            List.of(inputFluidHandler1, inputFluidHandler2, inputFluidHandler3),
                            outputItemHandler,
                            List.of(outputFluidHandler)))
                    .findFirst();
        } else {
            MixerRecipe recipeHandler = (MixerRecipe) this.recipeHandler.get();
            recipeEnergyCost = recipeHandler.getTotalEnergy();

            if (energyConsumed == 0) {
                recipeHandler.consumeIngredients(combinedInputItemHandler, List.of(inputFluidHandler1, inputFluidHandler2, inputFluidHandler3));
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
                        .getAllRecipesFor(MixerRecipe.TYPE)
                        .stream()
                        .filter(r -> r.recipeMatch(
                                combinedInputItemHandler,
                                List.of(inputFluidHandler1, inputFluidHandler2, inputFluidHandler3),
                                outputItemHandler,
                                List.of(outputFluidHandler)))
                        .findFirst();
            }
        }

        PacketRegistries.sendToClients(new MixerPacket(
                energyCapacity,
                energyStored,
                energyConsumeRate,
                energyConsumed,
                recipeEnergyCost,
                isStructureValid,
                blockPos,
                recipeHandler.map(ModRecipe::getRecipe).orElse(null)
        ));
    }





    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biotech." + MachineRegistries.MIXER.id());
    }
}