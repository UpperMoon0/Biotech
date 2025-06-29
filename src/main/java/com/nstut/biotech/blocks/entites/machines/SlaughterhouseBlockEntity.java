package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.network.SlaughterhousePacket;
import com.nstut.biotech.recipes.SlaughterhouseRecipe;
import com.nstut.biotech.views.machines.menu.SlaughterhouseMenu;
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

public class SlaughterhouseBlockEntity extends MachineBlockEntity {

    private ItemInputHatchBlockEntity itemInputHatch1;
    private ItemInputHatchBlockEntity itemInputHatch2;
    private ItemOutputHatchBlockEntity itemOutputHatch;
    private EnergyInputHatchBlockEntity energyInputHatch;
    private FluidInputHatchBlockEntity fluidInputHatch;

    public SlaughterhouseBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistries.SLAUGHTERHOUSE.blockEntity().get(), pos, state, 2, 1, 0);
    }
    @Override
    public AbstractContainerMenu createMenu(int pContainerId,
                                            @NotNull Inventory pPlayerInventory,
                                            @NotNull Player pPlayer) {
        return new SlaughterhouseMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        IItemHandler combinedInputItemHandler = new CombinedInvWrapper(
                (IItemHandlerModifiable) itemInputHatch1.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new),
                (IItemHandlerModifiable) itemInputHatch2.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new)
        );
        IItemHandler outputItemHandler = itemOutputHatch.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
        IFluidHandler inputFluidHandler = fluidInputHatch.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(NullPointerException::new);
        IEnergyStorage energyStorage = energyInputHatch.getCapability(ForgeCapabilities.ENERGY).orElseThrow(NullPointerException::new);

        int energyCapacity = energyInputHatch.ENERGY_CAPACITY;
        int energyStored = energyStorage.getEnergyStored();
        int energyConsumeRate = energyInputHatch.ENERGY_THROUGHPUT;
        int fluidCapacity = FluidInputHatchBlockEntity.TANK_CAPACITY;
        FluidStack fluidStored = inputFluidHandler.getFluidInTank(0);

        if (recipeHandler.isEmpty()) {
            energyConsumed = 0;
            recipeHandler = level
                    .getRecipeManager()
                    .getAllRecipesFor(SlaughterhouseRecipe.TYPE)
                    .stream().filter(r -> r.recipeMatch(
                            combinedInputItemHandler,
                            List.of(inputFluidHandler),
                            outputItemHandler,
                            null))
                    .findFirst();
        } else {
            SlaughterhouseRecipe recipeHandler = (SlaughterhouseRecipe) this.recipeHandler.get();
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
                        .getAllRecipesFor(SlaughterhouseRecipe.TYPE)
                        .stream().filter(r -> r.recipeMatch(
                                combinedInputItemHandler,
                                List.of(inputFluidHandler),
                                outputItemHandler,
                                null))
                        .findFirst();            }
        }

        PacketRegistries.sendToClients(new SlaughterhousePacket(
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
    protected void setHatches(BlockPos blockPos, Level level) {
        super.setHatches(blockPos, level);
        itemInputHatch1 = (ItemInputHatchBlockEntity) getHatchBlockEntity("item_input_1");
        itemInputHatch2 = (ItemInputHatchBlockEntity) getHatchBlockEntity("item_input_2");
        itemOutputHatch = (ItemOutputHatchBlockEntity) getHatchBlockEntity("item_output");
        energyInputHatch = (EnergyInputHatchBlockEntity) getHatchBlockEntity("energy_input");
        fluidInputHatch = (FluidInputHatchBlockEntity) getHatchBlockEntity("fluid_input");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biotech.slaughterhouse");
    }
}