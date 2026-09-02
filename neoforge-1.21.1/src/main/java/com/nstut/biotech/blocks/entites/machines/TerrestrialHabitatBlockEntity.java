package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.blocks.BlockRegistries;
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
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class TerrestrialHabitatBlockEntity extends MachineBlockEntity {
    private ItemInputHatchBlockEntity itemInputHatch1;
    private ItemInputHatchBlockEntity itemInputHatch2;
    private ItemInputHatchBlockEntity itemInputHatch3;
    private ItemOutputHatchBlockEntity itemOutputHatch;
    private EnergyInputHatchBlockEntity energyInputHatch;
    private FluidInputHatchBlockEntity fluidInputHatch;

    public TerrestrialHabitatBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistries.TERRESTRIAL_HABITAT.blockEntity().get(), pos, state, 3, 1, 0);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new TerrestrialHabitatMenu(containerId, inventory, this);
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        IItemHandler inputItems = new CombinedInvWrapper(
                itemInputHatch1.getInternalItemStorage(),
                itemInputHatch2.getInternalItemStorage(),
                itemInputHatch3.getInternalItemStorage());
        IItemHandler outputItems = itemOutputHatch.getInternalItemStorage();
        IFluidHandler inputFluid = fluidInputHatch.getInternalTank();
        IEnergyStorage energy = energyInputHatch.getInternalEnergyStorage();

        processRecipeTransaction(
                level,
                TerrestrialHabitatRecipe.TYPE,
                inputItems,
                List.of(inputFluid),
                outputItems,
                List.of(),
                energy,
                EnergyInputHatchBlockEntity.ENERGY_THROUGHPUT);

        if (level instanceof ServerLevel serverLevel && level.getGameTime() % 5L == 0L) {
            PacketRegistries.sendToTrackingChunk(serverLevel, blockPos, new TerrestrialHabitatPacket(
                    EnergyInputHatchBlockEntity.ENERGY_CAPACITY,
                    energy.getEnergyStored(),
                    EnergyInputHatchBlockEntity.ENERGY_THROUGHPUT,
                    energyConsumed,
                    recipeEnergyCost,
                    FluidInputHatchBlockEntity.TANK_CAPACITY,
                    inputFluid.getFluidInTank(0).copy(),
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
        MultiblockBlock a = new MultiblockBlock(MachineRegistries.TERRESTRIAL_HABITAT.block().get(), Map.of("facing", "south")),
                b = new MultiblockBlock(BlockRegistries.BIOTECH_MACHINE_CASING.get(), Map.of()),
                c = new MultiblockBlock(BlockRegistries.ITEM_INPUT_HATCH.get(), Map.of("facing", "west")),
                d = new MultiblockBlock(BlockRegistries.ITEM_OUTPUT_HATCH.get(), Map.of("facing", "east")),
                e = new MultiblockBlock(BlockRegistries.FLUID_INPUT_HATCH.get(), Map.of("facing", "north")),
                f = new MultiblockBlock(BlockRegistries.ENERGY_INPUT_HATCH.get(), Map.of("facing", "north")),
                g = new MultiblockBlock(Blocks.YELLOW_CONCRETE, Map.of()),
                h = new MultiblockBlock(Blocks.YELLOW_STAINED_GLASS, Map.of()),
                i = new MultiblockBlock(Blocks.GLOWSTONE, Map.of()),
                j = new MultiblockBlock(Blocks.GRASS_BLOCK, Map.of());

        MultiblockBlock[][][] blockArray = new MultiblockBlock[][][]{
                {
                        {g, h, h, g, null, null, null},
                        {h, i, i, h, null, null, null},
                        {h, i, i, h, null, null, null},
                        {g, h, h, g, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null}
                },
                {
                        {b, b, b, b, null, null, null},
                        {b, null, null, b, null, null, null},
                        {b, null, null, b, null, null, null},
                        {b, b, b, b, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null}
                },
                {
                        {b, b, b, b, null, null, null},
                        {b, null, null, b, null, null, null},
                        {b, null, null, b, null, null, null},
                        {b, null, null, b, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null}
                },
                {
                        {b, b, b, b, h, h, g},
                        {b, null, null, b, null, null, h},
                        {b, null, null, b, null, null, h},
                        {b, null, null, b, null, null, h},
                        {h, null, null, null, null, null, h},
                        {h, null, null, null, null, null, h},
                        {g, h, h, h, h, h, g}
                },
                {
                        {b, b, b, b, b, b, b},
                        {b, null, null, b, null, null, b},
                        {b, null, null, b, null, null, b},
                        {b, null, null, b, null, null, b},
                        {b, null, null, null, null, null, b},
                        {b, null, null, null, null, null, b},
                        {b, b, b, a, b, b, b}
                },
                {
                        {b, e, b, f, b, b, b},
                        {c, b, b, b, j, j, b},
                        {b, b, b, b, j, j, b},
                        {c, b, b, b, j, j, d},
                        {b, j, j, j, j, j, b},
                        {c, j, j, j, j, j, b},
                        {b, b, b, b, b, b, b}
                }
        };

        return new MultiblockPattern(blockArray);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biotech.terrestrial_habitat");
    }
}
