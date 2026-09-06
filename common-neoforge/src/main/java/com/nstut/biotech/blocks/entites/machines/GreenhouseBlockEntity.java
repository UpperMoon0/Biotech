package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.GreenhousePacket;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.recipes.GreenhouseRecipe;
import com.nstut.biotech.views.machines.menu.GreenhouseMenu;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GreenhouseBlockEntity extends MachineBlockEntity {
    private ItemInputHatchBlockEntity itemInputHatch;
    private ItemOutputHatchBlockEntity itemOutputHatch;
    private EnergyInputHatchBlockEntity energyInputHatch;
    private FluidInputHatchBlockEntity fluidInputHatch;

    public GreenhouseBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistries.GREENHOUSE.blockEntity().get(), pos, state, 3, 0, 0);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new GreenhouseMenu(containerId, inventory, this);
    }

    @Override
    protected void processRecipe(Level level, BlockPos blockPos) {
        IItemHandler inputItems = new CombinedInvWrapper(itemInputHatch.getInternalItemStorage());
        IItemHandler outputItems = itemOutputHatch.getInternalItemStorage();
        IFluidHandler inputFluid = fluidInputHatch.getInternalTank();
        IEnergyStorage energy = energyInputHatch.getInternalEnergyStorage();

        processRecipeTransaction(
                level,
                GreenhouseRecipe.TYPE,
                inputItems,
                List.of(inputFluid),
                outputItems,
                List.of(),
                energy,
                EnergyInputHatchBlockEntity.ENERGY_THROUGHPUT,
                Comparator.comparingInt(recipe -> recipe.getItemIngredients().size()));

        if (level instanceof ServerLevel serverLevel && level.getGameTime() % 5L == 0L) {
            PacketRegistries.sendToTrackingChunk(serverLevel, blockPos, new GreenhousePacket(
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
                new Vec3i(-3, 0, -3),
                new Vec3i(3, 0, -3),
                new Vec3i(0, 0, -6),
                new Vec3i(-2, 0, -6)
        };

        for (int i = 0; i < southOffset.length; i++) {
            Vec3i rotatedOffset = rotateHatchesOffset(southOffset[i], facing);
            BlockPos hatchPos = blockPos.offset(rotatedOffset);
            switch (i) {
                case 0 -> itemInputHatch = (ItemInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 1 -> itemOutputHatch = (ItemOutputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 2 -> energyInputHatch = (EnergyInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 3 -> fluidInputHatch = (FluidInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                default -> throw new IllegalStateException("Unexpected hatch index " + i);
            }
        }
    }

    @Override
    public MultiblockPattern getMultiblockPattern() {
        MultiblockBlock b = new MultiblockBlock(Blocks.PRISMARINE_BRICK_SLAB, Map.of("waterlogged", "false", "type", "bottom"));
        MultiblockBlock c = new MultiblockBlock(Blocks.LIME_CONCRETE);
        MultiblockBlock d = new MultiblockBlock(Blocks.GLOWSTONE);
        MultiblockBlock e = new MultiblockBlock(BlockRegistries.BIOTECH_MACHINE_CASING.get());
        MultiblockBlock f = new MultiblockBlock(Blocks.PRISMARINE_BRICK_SLAB, Map.of("waterlogged", "false", "type", "top"));
        MultiblockBlock g = new MultiblockBlock(Blocks.LIME_STAINED_GLASS);
        MultiblockBlock h = new MultiblockBlock(Blocks.DIORITE_WALL, Map.of("waterlogged", "false"));
        MultiblockBlock i = new MultiblockBlock(Blocks.PRISMARINE_BRICK_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "facing", "north"));
        MultiblockBlock j = new MultiblockBlock(Blocks.PRISMARINE_BRICK_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "facing", "south"));
        MultiblockBlock k = new MultiblockBlock(Blocks.PRISMARINE_BRICK_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "facing", "east"));
        MultiblockBlock l = new MultiblockBlock(Blocks.PRISMARINE_BRICK_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "facing", "west"));
        MultiblockBlock m = new MultiblockBlock(MachineRegistries.GREENHOUSE.block().get(), Map.of("facing", "south", "operating", "false"));
        MultiblockBlock n = new MultiblockBlock(Blocks.FARMLAND);
        MultiblockBlock o = new MultiblockBlock(BlockRegistries.ITEM_OUTPUT_HATCH.get(), Map.of("facing", "east"));
        MultiblockBlock p = new MultiblockBlock(Blocks.WATER, Map.of("level", "0"));
        MultiblockBlock q = new MultiblockBlock(BlockRegistries.ITEM_INPUT_HATCH.get(), Map.of("facing", "west"));
        MultiblockBlock r = new MultiblockBlock(BlockRegistries.ENERGY_INPUT_HATCH.get(), Map.of("facing", "north"));
        MultiblockBlock s = new MultiblockBlock(BlockRegistries.FLUID_INPUT_HATCH.get(), Map.of("facing", "north"));

        MultiblockBlock[][][] blockArray = new MultiblockBlock[][][] {
                {
                        {null, null, null, null, null, null, null},
                        {null, null, null, b, null, null, null},
                        {null, null, c, c, c, null, null},
                        {null, b, c, d, c, b, null},
                        {null, null, c, c, c, null, null},
                        {null, null, null, b, null, null, null},
                        {null, null, null, null, null, null, null},
                },
                {
                        {null, null, b, b, b, null, null},
                        {null, e, e, e, e, e, null},
                        {b, e, null, null, null, e, b},
                        {b, e, null, null, null, e, b},
                        {b, e, null, null, null, e, b},
                        {null, e, e, e, e, e, null},
                        {null, null, b, b, b, null, null},
                },
                {
                        {b, f, null, null, null, f, b},
                        {f, e, g, g, g, e, f},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {f, e, g, g, g, e, f},
                        {b, f, null, null, null, f, b},
                },
                {
                        {h, null, null, null, null, null, h},
                        {null, e, g, g, g, e, null},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {null, e, g, g, g, e, null},
                        {h, null, null, null, null, null, h},
                },
                {
                        {c, l, null, null, null, k, c},
                        {i, e, g, g, g, e, i},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {null, g, null, null, null, g, null},
                        {j, e, g, g, g, e, j},
                        {c, l, null, null, null, k, c},
                },
                {
                        {e, s, e, r, e, e, e},
                        {e, e, e, e, e, e, e},
                        {e, e, n, n, n, e, e},
                        {q, e, n, p, n, e, o},
                        {e, e, n, n, n, e, e},
                        {e, e, e, e, e, e, e},
                        {e, e, e, m, e, e, e},
                }
        };

        return new MultiblockPattern(blockArray);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biotech.greenhouse");
    }
}
