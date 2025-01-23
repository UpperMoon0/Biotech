package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.FermenterPacket;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.recipes.FermenterRecipe;
import com.nstut.biotech.views.machines.menu.FermenterMenu;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.ModRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
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

public class FermenterBlockEntity extends MachineBlockEntity {

    private ItemInputHatchBlockEntity itemInputHatch;
    private ItemOutputHatchBlockEntity itemOutputHatch;
    private EnergyInputHatchBlockEntity energyInputHatch;
    private FluidInputHatchBlockEntity fluidInputHatch;

    public FermenterBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistries.FERMENTER.blockEntity().get(), pos, state, 3, 1, 0);
    }
    @Override
    public AbstractContainerMenu createMenu(int pContainerId,
                                            @NotNull Inventory pPlayerInventory,
                                            @NotNull Player pPlayer) {
        return new FermenterMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        IItemHandler combinedInputItemHandler = new CombinedInvWrapper(
                (IItemHandlerModifiable) itemInputHatch.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new)
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
                    .getAllRecipesFor(FermenterRecipe.TYPE)
                    .stream()
                    .filter(r -> r.recipeMatch(
                            combinedInputItemHandler,
                            List.of(inputFluidHandler),
                            outputItemHandler,
                            null))
                    .findFirst();
        } else {
            FermenterRecipe recipeHandler = (FermenterRecipe) this.recipeHandler.get();
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
                        .getAllRecipesFor(FermenterRecipe.TYPE)
                        .stream()
                        .filter(r -> r.recipeMatch(
                                combinedInputItemHandler,
                                List.of(inputFluidHandler),
                                outputItemHandler,
                                null))
                        .findFirst();
            }
        }

        PacketRegistries.sendToClients(new FermenterPacket(
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
                new Vec3i(-2, 1, -5),
                new Vec3i(2, 1, -5),
                new Vec3i(1, -1, -8),
                new Vec3i(-1, -1, -8)
        };

        // Rotate the south offset and get the hatches
        for (int i = 0; i < southOffset.length; i++) {
            Vec3i rotatedOffset = rotateHatchesOffset(southOffset[i], facing);
            BlockPos hatchPos = blockPos.offset(rotatedOffset);

            switch (i) {
                case 0 -> itemInputHatch = (ItemInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 1 -> itemOutputHatch = (ItemOutputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 2 -> energyInputHatch = (EnergyInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 3 -> fluidInputHatch = (FluidInputHatchBlockEntity) level.getBlockEntity(hatchPos);
            }
        }
    }

    @Override
    public MultiblockPattern getMultiblockPattern() {
        MultiblockBlock b = new MultiblockBlock(Blocks.IRON_BLOCK, Map.of()),
        c = new MultiblockBlock(BlockRegistries.BIOTECH_MACHINE_CASING.get(), Map.of()),
        e = new MultiblockBlock(BlockRegistries.ENERGY_INPUT_HATCH.get(), Map.of("facing", "north")),
        d = new MultiblockBlock(BlockRegistries.FLUID_INPUT_HATCH.get(), Map.of("facing", "north")),
        f = new MultiblockBlock(Blocks.SMOOTH_STONE_SLAB, Map.of("waterlogged", "false", "type", "bottom")),
        h = new MultiblockBlock(Blocks.GLOWSTONE, Map.of()),
        i = new MultiblockBlock(MachineRegistries.FERMENTER.block().get(), Map.of("facing", "south", "operating", "false")),
        k = new MultiblockBlock(BlockRegistries.ITEM_OUTPUT_HATCH.get(), Map.of("facing", "east")),
        j = new MultiblockBlock(BlockRegistries.ITEM_INPUT_HATCH.get(), Map.of("facing", "west")),
        l = new MultiblockBlock(Blocks.BROWN_STAINED_GLASS, Map.of()),
        m = new MultiblockBlock(Blocks.BROWN_TERRACOTTA, Map.of());

        MultiblockBlock[][][] blockArray = new MultiblockBlock[][][] {
            {
                    {null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null},
                    {null, null, c, c, c, null, null},
                    {null, c, c, c, c, c, null},
                    {null, c, c, h, c, c, null},
                    {null, c, c, c, c, c, null},
                    {null, null, c, c, c, null, null},
                    {null, null, null, null, null, null, null},
            },
            {
                    {null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null},
                    {null, null, l, l, l, null, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, null, l, l, l, null, null},
                    {null, null, null, null, null, null, null},
            },
            {
                    {null, c, c, null, c, c, null},
                    {c, c, c, c, c, c, c},
                    {c, c, c, c, c, c, c},
                    {null, c, c, null, c, c, null},
                    {null, null, l, l, l, null, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, null, l, l, l, null, null},
                    {null, null, null, null, null, null, null},
            },
            {
                    {null, l, l, null, l, l, null},
                    {l, m, m, l, m, m, l},
                    {l, m, m, l, m, m, l},
                    {null, l, l, null, l, l, null},
                    {null, null, l, l, l, null, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, null, l, l, l, null, null},
                    {null, null, null, null, null, null, null},
            },
            {
                    {null, l, l, null, l, l, null},
                    {l, m, m, l, m, m, l},
                    {l, m, m, l, m, m, l},
                    {null, l, l, null, l, l, null},
                    {null, null, l, l, l, null, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, null, l, l, l, null, null},
                    {null, null, null, null, null, null, null},
            },
            {
                    {null, l, l, null, l, l, null},
                    {l, m, m, l, m, m, l},
                    {l, m, m, l, m, m, l},
                    {null, l, l, null, l, l, null},
                    {null, null, l, l, l, null, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, l, m, m, m, l, null},
                    {null, null, l, l, l, null, null},
                    {null, null, null, null, null, null, null},
            },
            {
                    {null, c, c, null, c, c, null},
                    {c, c, c, c, c, c, c},
                    {c, c, c, c, c, c, c},
                    {null, c, c, null, c, c, null},
                    {null, null, c, c, c, null, null},
                    {null, c, c, c, c, c, null},
                    {null, c, c, c, c, c, null},
                    {null, c, c, c, c, c, null},
                    {null, null, c, c, c, null, null},
                    {null, null, null, null, null, null, null},
            },
            {
                    {null, null, null, null, null, null, null},
                    {null, c, c, h, c, c, null},
                    {null, h, c, c, c, h, null},
                    {null, h, c, c, c, h, null},
                    {null, c, c, c, c, c, null},
                    {null, h, c, c, c, h, null},
                    {null, h, c, c, c, h, null},
                    {null, c, c, c, c, c, null},
                    {null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null},
            },
            {
                    {null, null, null, null, null, null, null},
                    {null, c, c, h, c, c, null},
                    {b, c, null, null, null, c, b},
                    {b, c, null, null, null, c, b},
                    {null, j, null, null, null, k, null},
                    {b, c, null, null, null, c, b},
                    {b, c, null, null, null, c, b},
                    {null, c, null, null, null, c, null},
                    {null, c, c, c, c, c, null},
                    {null, null, null, null, null, null, null},
            },
            {
                    {null, null, null, null, null, null, null},
                    {b, c, c, h, c, c, b},
                    {f, c, null, null, null, c, f},
                    {f, c, null, null, null, c, f},
                    {b, c, null, null, null, c, b},
                    {f, c, null, null, null, c, f},
                    {f, c, null, null, null, c, f},
                    {b, c, null, null, null, c, b},
                    {null, c, null, null, null, c, null},
                    {null, c, c, i, c, c, null},
            },
            {
                    {null, null, null, null, null, null, null},
                    {b, c, d, c, e, c, b},
                    {f, c, c, c, c, c, f},
                    {f, c, c, c, c, c, f},
                    {b, c, c, c, c, c, b},
                    {f, c, c, c, c, c, f},
                    {f, c, c, c, c, c, f},
                    {b, c, c, c, c, c, b},
                    {null, c, c, c, c, c, null},
                    {null, c, c, c, c, c, null},
            },
        };

        return new MultiblockPattern(blockArray);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biotech." + MachineRegistries.FERMENTER.id());
    }
}