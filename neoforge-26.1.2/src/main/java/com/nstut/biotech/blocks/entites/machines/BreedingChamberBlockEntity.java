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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new BreedingChamberMenu(containerId, inventory, this);
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        IItemHandler inputItems = new CombinedInvWrapper(
                (IItemHandlerModifiable) itemInputHatch1.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new),
                (IItemHandlerModifiable) itemInputHatch2.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new),
                (IItemHandlerModifiable) itemInputHatch3.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new));
        IItemHandler outputItems = itemOutputHatch.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        IFluidHandler inputFluid = fluidInputHatch.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        IEnergyStorage energy = energyInputHatch.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);

        processRecipeTransaction(
                level,
                BreedingChamberRecipe.TYPE,
                inputItems,
                List.of(inputFluid),
                outputItems,
                List.of(),
                energy,
                EnergyInputHatchBlockEntity.ENERGY_THROUGHPUT);

        if (level instanceof ServerLevel serverLevel && level.getGameTime() % 5L == 0L) {
            FluidStack fluidStored = inputFluid.getFluidInTank(0).copy();
            PacketRegistries.sendToTrackingChunk(serverLevel, blockPos, new BreedingChamberPacket(
                    EnergyInputHatchBlockEntity.ENERGY_CAPACITY,
                    energy.getEnergyStored(),
                    EnergyInputHatchBlockEntity.ENERGY_THROUGHPUT,
                    energyConsumed,
                    recipeEnergyCost,
                    FluidInputHatchBlockEntity.TANK_CAPACITY,
                    fluidStored,
                    isStructureValid,
                    blockPos,
                    recipeHandler.map(ModRecipe::getRecipe).orElse(null)));
        }
    }

    @Override
    protected void setHatches(BlockPos blockPos, Level level) {
        Direction facing = getBlockState().getValue(getFacingProperty());
        Vec3i[] southOffset = {
                new Vec3i(-3, -1, -1),
                new Vec3i(-3, -1, -3),
                new Vec3i(-3, -1, -5),
                new Vec3i(3, -1, -3),
                new Vec3i(0, -1, -6),
                new Vec3i(-2, -1, -6)
        };

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
                default -> throw new IllegalStateException("Unexpected hatch index " + i);
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

        MultiblockBlock[][][] blockArray = new MultiblockBlock[][][]{
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
