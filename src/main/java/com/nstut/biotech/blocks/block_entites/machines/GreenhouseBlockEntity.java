package com.nstut.biotech.blocks.block_entites.machines;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.block_entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.block_entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.block_entites.hatches.ItemInputHatchBlockEntity;
import com.nstut.biotech.blocks.block_entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.network.GreenhousePacket;
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

import java.util.Comparator;
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
    public AbstractContainerMenu createMenu(int pContainerId,
                                            @NotNull Inventory pPlayerInventory,
                                            @NotNull Player pPlayer) {
        return new GreenhouseMenu(pContainerId, pPlayerInventory, this);
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
                greenhouseRecipe.consumeIngredients(combinedInputItemHandler, inputFluidHandler);
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
                .filter(recipe -> recipe.recipeMatch(combinedInputItemHandler, inputFluidHandler, outputItemHandler, null))
                .max(Comparator.comparingInt(recipe -> recipe.getItemIngredients().size()))
                .stream().findFirst();
    }

    @Override
    protected void setHatches(BlockPos blockPos, Level level) {
        Direction facing = getBlockState().getValue(getFacingProperty());

        // Define the south offset
        Vec3i[] southOffset = {
                new Vec3i(-3, 0, -3),
                new Vec3i(3, 0, -3),
                new Vec3i(0, 0, -6),
                new Vec3i(-2, 0, -6)
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
        MultiblockBlock b = new MultiblockBlock(Blocks.PRISMARINE_BRICK_SLAB, Map.of("waterlogged", "false", "type", "bottom"));
        MultiblockBlock c = new MultiblockBlock(Blocks.LIME_CONCRETE);
        MultiblockBlock d = new MultiblockBlock(Blocks.GLOWSTONE);
        MultiblockBlock e = new MultiblockBlock(BlockRegistries.BIOTECH_MACHINE_CASING.get());
        MultiblockBlock f = new MultiblockBlock(Blocks.PRISMARINE_BRICK_SLAB, Map.of("waterlogged", "false", "type", "top"));
        MultiblockBlock g = new MultiblockBlock(Blocks.LIME_STAINED_GLASS);
        MultiblockBlock h = new MultiblockBlock(Blocks.DIORITE_WALL, Map.of("east", "none", "waterlogged", "false", "south", "none", "north", "none", "west", "none", "up", "true"));
        MultiblockBlock i = new MultiblockBlock(Blocks.PRISMARINE_BRICK_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "straight", "facing", "north"));
        MultiblockBlock j = new MultiblockBlock(Blocks.PRISMARINE_BRICK_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "straight", "facing", "south"));
        MultiblockBlock k = new MultiblockBlock(Blocks.PRISMARINE_BRICK_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "straight", "facing", "east"));
        MultiblockBlock l = new MultiblockBlock(Blocks.PRISMARINE_BRICK_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "straight", "facing", "west"));
        MultiblockBlock m = new MultiblockBlock(MachineRegistries.GREENHOUSE.block().get(), Map.of("facing", "south", "operating", "false"));
        MultiblockBlock n = new MultiblockBlock(Blocks.FARMLAND, Map.of("moisture", "0"));
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