package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.network.GreenhousePacket;
import com.nstut.biotech.recipes.GreenhouseRecipe;
import com.nstut.biotech.views.machines.menu.GreenhouseMenu;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.recipes.ModRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import java.util.Comparator;
import java.util.List;

public class GreenhouseBlockEntity extends MachineBlockEntity {


    public GreenhouseBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistries.GREENHOUSE.blockEntity().get(), pos, state, 3, 0, 0);
    }
    @Override
    public AbstractContainerMenu createMenu(int pContainerId,
                                            @NotNull Inventory pPlayerInventory,
                                            @NotNull Player pPlayer) {
        return new GreenhouseMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        IItemHandler combinedInputItemHandler = new CombinedInvWrapper(
                (IItemHandlerModifiable) getItemInputHatch().getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new)
        );
        IItemHandler outputItemHandler = getItemOutputHatch().getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
        IFluidHandler inputFluidHandler = getFluidInputHatch().getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(NullPointerException::new);
        IEnergyStorage energyStorage = getEnergyInputHatch().getCapability(ForgeCapabilities.ENERGY).orElseThrow(NullPointerException::new);

        int energyCapacity = getEnergyInputHatch().ENERGY_CAPACITY;
        int energyStored = energyStorage.getEnergyStored();
        int energyConsumeRate = getEnergyInputHatch().ENERGY_THROUGHPUT;
        int fluidCapacity = FluidInputHatchBlockEntity.TANK_CAPACITY;
        FluidStack fluidStored = inputFluidHandler.getFluidInTank(0);

        // Attempt to find a new recipe if none is currently being processed
        if (recipeHandler.isEmpty()) {
            findMatchingRecipe(level, combinedInputItemHandler, inputFluidHandler, outputItemHandler);
            energyConsumed = 0; // Reset energy consumption for the new recipe
        }

        recipeHandler.ifPresent(recipe -> {
            GreenhouseRecipe greenhouseRecipe = (GreenhouseRecipe) recipe;
            recipeEnergyCost = greenhouseRecipe.getTotalEnergy();

            // Consume ingredients at the start of the recipe
            if (energyConsumed == 0) {
                greenhouseRecipe.consumeIngredients(combinedInputItemHandler, List.of(inputFluidHandler));
            }

            // Consume energy if available
            int energyToConsume = Math.min(energyConsumeRate, recipeEnergyCost - energyConsumed);
            if (energyToConsume > 0 && energyStored >= energyToConsume) {
                energyConsumed += energyToConsume;
                energyStorage.extractEnergy(energyToConsume, false);
            }

            // Complete the recipe when enough energy has been consumed
            if (energyConsumed >= recipeEnergyCost) {
                energyConsumed = 0;
                greenhouseRecipe.assemble(outputItemHandler, null);

                // Attempt to find the next recipe
                findMatchingRecipe(level, combinedInputItemHandler, inputFluidHandler, outputItemHandler);
            }
        });

        // Send packet to clients
        PacketRegistries.sendToClients(new GreenhousePacket(
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

    /**
     * Finds and sets the best matching recipe for the given input/output handlers.
     */
    private void findMatchingRecipe(Level level, IItemHandler combinedInputItemHandler, IFluidHandler inputFluidHandler, IItemHandler outputItemHandler) {
        recipeHandler = level.getRecipeManager().getAllRecipesFor(GreenhouseRecipe.TYPE).stream()
                .filter(recipe -> recipe.recipeMatch(
                        combinedInputItemHandler,
                        List.of(inputFluidHandler),
                        outputItemHandler,
                        null))
                .max(Comparator.comparingInt(recipe -> recipe.getItemIngredients().size()))
                .stream().findFirst();
    }


    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biotech.greenhouse");
    }
}