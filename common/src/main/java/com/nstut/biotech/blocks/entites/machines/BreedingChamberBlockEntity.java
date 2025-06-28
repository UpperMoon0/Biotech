package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.BreedingChamberPacket;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.recipes.BreedingChamberRecipe;
import com.nstut.biotech.views.machines.menu.BreedingChamberMenu;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.ModRecipe;
import dev.architectury.fluid.FluidStack;
import dev.architectury.transfer.energy.EnergyStorage;
import dev.architectury.transfer.fluid.FluidStorage;
import dev.architectury.transfer.item.ItemStorage;
import dev.architectury.transfer.item.ItemTransfer;
import dev.architectury.transfer.storage.Storage;
import dev.architectury.transfer.storage.item.CombinedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BreedingChamberBlockEntity extends MachineBlockEntity {

    private ItemInputHatchBlockEntity itemInputHatch1;
    private ItemInputHatchBlockEntity itemInputHatch2;
    private ItemInputHatchBlockEntity itemInputHatch3;
    private ItemOutputHatchBlockEntity itemOutputHatch;
    private EnergyInputHatchBlockEntity energyInputHatch;
    private FluidInputHatchBlockEntity fluidInputHatch;

    public BreedingChamberBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistries.BREEDING_CHAMBER.blockEntity().get(), pos, state, 3, 1, 0);
    }
    @Override
    public AbstractContainerMenu createMenu(int pContainerId,
                                            @NotNull Inventory pPlayerInventory,
                                            @NotNull Player pPlayer) {
        // Ensure hatches are not null before accessing capabilities
        long energyCapacity = energyInputHatch != null ? energyInputHatch.ENERGY_CAPACITY : 0;
        long currentEnergy = 0;
        if (energyInputHatch != null) {
            Storage<dev.architectury.transfer.energy.EnergyVariant> energyStorage = energyInputHatch.getHandlerStorage().get(EnergyStorage.SIDED, getFacingForHatch(energyInputHatch));
            if (energyStorage != null) {
                currentEnergy = energyStorage.getAmount();
            }
        }
        long energyThroughput = energyInputHatch != null ? energyInputHatch.ENERGY_THROUGHPUT : 0;
        long fluidCapacity = fluidInputHatch != null ? FluidInputHatchBlockEntity.TANK_CAPACITY : 0;
        FluidStack currentFluid = FluidStack.empty();
        if (fluidInputHatch != null) {
            Storage<dev.architectury.fluid.FluidVariant> fluidStorage = fluidInputHatch.getHandlerStorage().get(FluidStorage.SIDED, getFacingForHatch(fluidInputHatch));
            if (fluidStorage != null && !fluidStorage.iterator().next().isResourceBlank()) {
                 currentFluid = FluidStack.create(fluidStorage.iterator().next().getResource().getFluid(), fluidStorage.iterator().next().getAmount());
            }
        }

        PacketRegistries.sendToClients(new BreedingChamberPacket(
                energyCapacity,
                currentEnergy,
                energyThroughput,
                energyConsumed,
                recipeEnergyCost,
                fluidCapacity,
                currentFluid,
                isStructureValid,
                worldPosition,
                recipeHandler.map(ModRecipe::getRecipe).orElse(null)
        ));
        return new BreedingChamberMenu(pContainerId, pPlayerInventory, this);
    }

    private Direction getFacingForHatch(@Nullable BlockEntity hatch) {
        if (hatch != null && hatch.getBlockState().hasProperty(IOHatchBlock.FACING)) {
            return hatch.getBlockState().getValue(IOHatchBlock.FACING);
        }
        return null;
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        if (itemInputHatch1 == null || itemInputHatch2 == null || itemInputHatch3 == null || itemOutputHatch == null || energyInputHatch == null || fluidInputHatch == null) {
            // Hatches are not yet loaded or set, defer processing
            return;
        }

        Storage<dev.architectury.transfer.item.ItemVariant> itemInput1 = itemInputHatch1.getHandlerStorage().get(ItemStorage.SIDED, getFacingForHatch(itemInputHatch1));
        Storage<dev.architectury.transfer.item.ItemVariant> itemInput2 = itemInputHatch2.getHandlerStorage().get(ItemStorage.SIDED, getFacingForHatch(itemInputHatch2));
        Storage<dev.architectury.transfer.item.ItemVariant> itemInput3 = itemInputHatch3.getHandlerStorage().get(ItemStorage.SIDED, getFacingForHatch(itemInputHatch3));
        Storage<dev.architectury.transfer.item.ItemVariant> outputItemHandler = itemOutputHatch.getHandlerStorage().get(ItemStorage.SIDED, getFacingForHatch(itemOutputHatch));
        Storage<dev.architectury.fluid.FluidVariant> inputFluidHandler = fluidInputHatch.getHandlerStorage().get(FluidStorage.SIDED, getFacingForHatch(fluidInputHatch));
        Storage<dev.architectury.transfer.energy.EnergyVariant> energyHandler = energyInputHatch.getHandlerStorage().get(EnergyStorage.SIDED, getFacingForHatch(energyInputHatch));


        if (itemInput1 == null || itemInput2 == null || itemInput3 == null || outputItemHandler == null || inputFluidHandler == null || energyHandler == null) {
            // A capability is missing, cannot process
            return;
        }

        Storage<dev.architectury.transfer.item.ItemVariant> combinedInputItemHandler = new CombinedStorage<>(List.of(itemInput1, itemInput2, itemInput3));


        long energyCapacity = energyInputHatch.ENERGY_CAPACITY;
        long energyStored = energyHandler.getAmount();
        long energyConsumeRate = energyInputHatch.ENERGY_THROUGHPUT;
        long fluidCapacity = FluidInputHatchBlockEntity.TANK_CAPACITY;
        FluidStack fluidStored = FluidStack.empty();
        if (!inputFluidHandler.iterator().next().isResourceBlank()){
            fluidStored = FluidStack.create(inputFluidHandler.iterator().next().getResource().getFluid(), inputFluidHandler.iterator().next().getAmount());
        }


        if (recipeHandler.isEmpty()) {
            energyConsumed = 0;
            Optional<BreedingChamberRecipe> foundRecipe = level
                    .getRecipeManager()
                    .getAllRecipesFor(BreedingChamberRecipe.TYPE)
                    .stream()
                    .filter(r -> r.recipeMatchArch(
                            combinedInputItemHandler,
                            List.of(inputFluidHandler),
                            outputItemHandler,
                            null))
                    .findFirst();
            if (foundRecipe.isPresent()) {
                recipeHandler = Optional.of(foundRecipe.get());
            }

        } else {
            BreedingChamberRecipe currentRecipe = (BreedingChamberRecipe) this.recipeHandler.get();
            recipeEnergyCost = currentRecipe.getTotalEnergy();

            if (energyConsumed == 0) {
                // Consume ingredients only if the recipe is new or was just completed
                if (currentRecipe.canCraftArch(combinedInputItemHandler, List.of(inputFluidHandler), outputItemHandler, null)) {
                    currentRecipe.consumeIngredientsArch(combinedInputItemHandler, List.of(inputFluidHandler));
                } else {
                    // Ingredients no longer available, reset recipe
                    recipeHandler = Optional.empty();
                    energyConsumed = 0;
                    return;
                }
            }

            if (energyHandler.getAmount() >= energyConsumeRate) {
                long energyToConsume = Math.min(energyConsumeRate, recipeEnergyCost - energyConsumed);
                long extracted = energyHandler.extract(dev.architectury.transfer.energy.EnergyVariant.blank(), energyToConsume, dev.architectury.transfer.tx.Transaction.openOuter());
                energyConsumed += extracted;

            }

            if (energyConsumed >= recipeEnergyCost) {
                currentRecipe.assembleArch(outputItemHandler, null);
                energyConsumed = 0; // Reset for the next cycle
                 Optional<BreedingChamberRecipe> foundRecipe = level
                        .getRecipeManager()
                        .getAllRecipesFor(BreedingChamberRecipe.TYPE)
                        .stream()
                        .filter(r -> r.recipeMatchArch(
                                combinedInputItemHandler,
                                List.of(inputFluidHandler),
                                outputItemHandler,
                                null))
                        .findFirst();
                if (foundRecipe.isPresent()) {
                    recipeHandler = Optional.of(foundRecipe.get());
                } else {
                    recipeHandler = Optional.empty();
                }
            }
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
    protected void setHatches(BlockPos blockPos, Level level) {
        Direction facing = getBlockState().getValue(getFacingProperty());

        // Define the south offset
        Vec3i[] southOffset = {
                new Vec3i(-3, -1, -1),
                new Vec3i(-3, -1, -3),
                new Vec3i(-3, -1, -5),
                new Vec3i(3, -1, -3),
                new Vec3i(0, -1, -6),
                new Vec3i(-2, -1, -6)
        };

        // Rotate the south offset and get the hatches
        for (int i = 0; i < southOffset.length; i++) {
            Vec3i rotatedOffset = rotateHatchesOffset(southOffset[i], facing);
            BlockPos hatchPos = blockPos.offset(rotatedOffset);
            switch (i) {
                case 0 -> itemInputHatch1 = (ItemInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 1 -> itemInputHatch2 = (ItemInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 2 -> itemInputHatch3 = (ItemInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 3 -> itemOutputHatch = (ItemOutputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 4 -> energyInputHatch = (EnergyInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 5 -> fluidInputHatch = (FluidInputHatchBlockEntity) level.getBlockEntity(hatchPos);
            }
        }
    }

    @Override
    public MultiblockPattern getMultiblockPattern() {
        MultiblockBlock a = new MultiblockBlock(MachineRegistries.BREEDING_CHAMBER.block().get(), Map.of("facing", "south")),
                b = new MultiblockBlock(BlockRegistries.BIOTECH_MACHINE_CASING.get(), Map.of()),
                c = new MultiblockBlock(BlockRegistries.ITEM_INPUT_HATCH.get(), Map.of("facing", "west")),
                d = new MultiblockBlock(BlockRegistries.ITEM_OUTPUT_HATCH.get(), Map.of("facing", "east")),
                e = new MultiblockBlock(BlockRegistries.FLUID_INPUT_HATCH.get(), Map.of("facing", "north")),
                f = new MultiblockBlock(BlockRegistries.ENERGY_INPUT_HATCH.get(), Map.of("facing", "north")),
                g = new MultiblockBlock(Blocks.PINK_CONCRETE, Map.of()),
                h = new MultiblockBlock(Blocks.PINK_STAINED_GLASS, Map.of()),
                i = new MultiblockBlock(Blocks.GLOWSTONE, Map.of()),
                j = new MultiblockBlock(Blocks.GRASS_BLOCK, Map.of());

        MultiblockBlock[][][] blockArray =  new MultiblockBlock[][][]{
                {
                        {null, null, h, b, h, null, null},
                        {null, null, h, i, h, null, null},
                        {null, null, h, i, h, null, null},
                        {null, null, h, i, h, null, null},
                        {null, null, h, i, h, null, null},
                        {null, null, h, i, h, null, null},
                        {null, null, h, b, h, null, null}
                },
                {
                        {null, g, h, b, h, g, null},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {null, g, h, b, h, g, null}
                },
                {
                        {null, b, b, b, b, b, null},
                        {null, b, null, null, null, b, null},
                        {null, b, null, null, null, b, null},
                        {null, b, null, null, null, b, null},
                        {null, b, null, null, null, b, null},
                        {null, b, null, null, null, b, null},
                        {null, b, b, b, b, b, null}
                },
                {
                        {b, b, b, b, b, b, b},
                        {b, null, null, null, null, null, b},
                        {b, null, null, null, null, null, b},
                        {b, null, null, null, null, null, b},
                        {b, null, null, null, null, null, b},
                        {b, null, null, null, null, null, b},
                        {b, b, b, a, b, b, b}
                },
                {
                        {b, e, b, f, b, b, b},
                        {c, b, j, j, j, b, b},
                        {b, b, j, j, j, b, b},
                        {c, b, j, j, j, b, d},
                        {b, b, j, j, j, b, b},
                        {c, b, j, j, j, b, b},
                        {b, b, b, b, b, b, b}
                }
        };

        return new MultiblockPattern(blockArray);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biotech.breeding_chamber");
    }
}