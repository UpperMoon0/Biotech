package com.nstut.biotech.compat.jade;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidOutputHatchBlockEntity;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.HashSet;
import java.util.Set;

public enum MachineDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final Identifier UID = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "machine_status");

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MachineBlockEntity machine)) return;

        boolean structureValid = machine.checkMultiblock(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState());
        boolean operating = accessor.getBlockState().hasProperty(MachineBlock.OPERATING)
                && accessor.getBlockState().getValue(MachineBlock.OPERATING);
        tag.putBoolean("StructureValid", structureValid);
        tag.putBoolean("Operating", operating);

        CompoundTag machineData = machine.saveWithoutMetadata(accessor.getLevel().registryAccess());
        tag.putInt("EnergyConsumed", machineData.getIntOr("energyConsumed", 0));
        tag.putInt("RecipeEnergyCost", machineData.getIntOr("recipeEnergyCost", 0));

        if (!structureValid) return;
        collectHatchData(tag, accessor, machine);
    }

    private static void collectHatchData(CompoundTag tag, BlockAccessor accessor, MachineBlockEntity machine) {
        MultiblockPattern multiblock = machine.getMultiblockPattern();
        MultiblockBlock[][][] pattern = multiblock.getPattern();
        Set<BlockPos> visited = new HashSet<>();
        int fluidIndex = 0;
        long energyStored = 0;
        long energyCapacity = 0;

        for (int patternY = 0; patternY < pattern.length; patternY++) {
            MultiblockBlock[][] layer = pattern[patternY];
            for (int z = 0; z < layer.length; z++) {
                for (int x = 0; x < layer[z].length; x++) {
                    if (layer[z][x] == null) continue;
                    BlockPos pos = MultiblockPattern.rotateBlockPos(
                            accessor.getPosition(),
                            machine.getSouthOffsetX(), machine.getSouthOffsetY(), machine.getSouthOffsetZ(),
                            pattern.length, layer.length, x, patternY, z, accessor.getBlockState());
                    if (!visited.add(pos)) continue;
                    BlockEntity blockEntity = accessor.getLevel().getBlockEntity(pos);
                    if (blockEntity instanceof EnergyInputHatchBlockEntity hatch) {
                        IEnergyStorage storage = hatch.getInternalEnergyStorage();
                        energyStored += storage.getEnergyStored();
                        energyCapacity += storage.getMaxEnergyStored();
                    } else if (blockEntity instanceof FluidInputHatchBlockEntity hatch) {
                        fluidIndex = collectFluid(tag, hatch.getInternalTank(), fluidIndex, false);
                    } else if (blockEntity instanceof FluidOutputHatchBlockEntity hatch) {
                        fluidIndex = collectFluid(tag, hatch.getInternalTank(), fluidIndex, true);
                    }
                }
            }
        }

        tag.putLong("EnergyStored", energyStored);
        tag.putLong("EnergyCapacity", energyCapacity);
        tag.putInt("FluidCount", fluidIndex);
    }

    private static int collectFluid(CompoundTag tag, IFluidHandler handler, int index, boolean output) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack fluid = handler.getFluidInTank(tank);
            int capacity = handler.getTankCapacity(tank);
            if (fluid.isEmpty() || capacity <= 0) continue;
            tag.putString("FluidName" + index, fluid.getHoverName().getString());
            tag.putString("FluidId" + index, BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            tag.putInt("FluidAmount" + index, fluid.getAmount());
            tag.putInt("FluidCapacity" + index, capacity);
            tag.putBoolean("FluidOutput" + index, output);
            index++;
        }
        return index;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
