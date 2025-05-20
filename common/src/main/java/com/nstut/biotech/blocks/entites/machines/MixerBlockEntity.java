package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.*;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.MixerPacket;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.recipes.MixerRecipe;
import com.nstut.biotech.views.machines.menu.MixerMenu;
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
import net.minecraft.world.level.material.Fluid;
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

public class MixerBlockEntity extends MachineBlockEntity {

    private ItemInputHatchBlockEntity itemInputHatch;
    private ItemOutputHatchBlockEntity itemOutputHatch;
    private EnergyInputHatchBlockEntity energyInputHatch;
    private FluidInputHatchBlockEntity fluidInputHatch1;
    private FluidInputHatchBlockEntity fluidInputHatch2;
    private FluidInputHatchBlockEntity fluidInputHatch3;
    private FluidOutputHatchBlockEntity fluidOutputHatch;

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistries.MIXER.blockEntity().get(), pos, state, 2, 1, 1);
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
    protected void setHatches(BlockPos blockPos, Level level) {
        Direction facing = getBlockState().getValue(getFacingProperty());

        // Define the south offset
        Vec3i[] southOffset = {
                new Vec3i(-1, 2, -1),
                new Vec3i(8, -1, 0),
                new Vec3i(0, 2, -2),
                new Vec3i(-2, 8, 0),
                new Vec3i(-2, 8, -1),
                new Vec3i(-2, 8, -2),
                new Vec3i(8, -1, -2),
        };

        // Rotate the south offset and get the hatches
        for (int i = 0; i < southOffset.length; i++) {
            Vec3i rotatedOffset = rotateHatchesOffset(southOffset[i], facing);
            BlockPos hatchPos = blockPos.offset(rotatedOffset);

            switch (i) {
                case 0 -> itemInputHatch = (ItemInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 1 -> itemOutputHatch = (ItemOutputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 2 -> energyInputHatch = (EnergyInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 3 -> fluidInputHatch1 = (FluidInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 4 -> fluidInputHatch2 = (FluidInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 5 -> fluidInputHatch3 = (FluidInputHatchBlockEntity) level.getBlockEntity(hatchPos);
                case 6 -> fluidOutputHatch = (FluidOutputHatchBlockEntity) level.getBlockEntity(hatchPos);
            }
        }
    }

    @Override
    public MultiblockPattern getMultiblockPattern() {
        MultiblockBlock b = new MultiblockBlock(Blocks.MUD_BRICKS, Map.of()),
                        c = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "outer_left", "facing", "west")),
                        d = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "straight", "facing", "south")),
                        e = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "outer_left", "facing", "south")),
                        f = new MultiblockBlock(BlockRegistries.FLUID_OUTPUT_HATCH.get(), Map.of("facing", "east")),
                        g = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "straight", "facing", "west")),
                        h = new MultiblockBlock(BlockRegistries.BIOTECH_MACHINE_CASING.get(), Map.of()),
                        i = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "straight", "facing", "east")),
                        j = new MultiblockBlock(Blocks.GLOWSTONE, Map.of()),
                        k = new MultiblockBlock(BlockRegistries.ITEM_OUTPUT_HATCH.get(), Map.of("facing", "east")),
                        l = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "outer_right", "facing", "west")),
                        m = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "straight", "facing", "north")),
                        n = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "bottom", "waterlogged", "false", "shape", "outer_left", "facing", "east")),
                        o = new MultiblockBlock(Blocks.PURPLE_STAINED_GLASS, Map.of()),
                        p = new MultiblockBlock(MachineRegistries.MIXER.block().get(), Map.of("facing", "south", "operating", "false")),
                        q = new MultiblockBlock(Blocks.PURPLE_CONCRETE, Map.of()),
                        r = new MultiblockBlock(Blocks.IRON_BLOCK, Map.of()),
                        s = new MultiblockBlock(BlockRegistries.ENERGY_INPUT_HATCH.get(), Map.of("facing", "north")),
                        t = new MultiblockBlock(BlockRegistries.ITEM_INPUT_HATCH.get(), Map.of("facing", "west")),
                        u = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "top", "waterlogged", "false", "shape", "outer_left", "facing", "west")),
                        v = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "top", "waterlogged", "false", "shape", "straight", "facing", "south")),
                        w = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "top", "waterlogged", "false", "shape", "outer_right", "facing", "east")),
                        x = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "top", "waterlogged", "false", "shape", "straight", "facing", "west")),
                        y = new MultiblockBlock(Blocks.WARPED_PLANKS, Map.of()),
                        z = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "top", "waterlogged", "false", "shape", "straight", "facing", "east")),
                        aa = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "top", "waterlogged", "false", "shape", "outer_right", "facing", "west")),
                        ab = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "top", "waterlogged", "false", "shape", "straight", "facing", "north")),
                        ac = new MultiblockBlock(Blocks.WARPED_STAIRS, Map.of("half", "top", "waterlogged", "false", "shape", "outer_left", "facing", "east")),
                        ad = new MultiblockBlock(BlockRegistries.FLUID_INPUT_HATCH.get(), Map.of("facing", "west"));

        MultiblockBlock[][][] blockArray = new MultiblockBlock[][][] {
            {
                    {null, null, null, null, null, null, null, null, null, null, null, null},
                    {ad, h, h, h, h, h, h, h, h, h, h, h},
                    {ad, h, h, h, h, h, h, h, h, h, h, h},
                    {ad, h, h, h, h, h, h, h, h, h, h, h},
                    {null, null, null, null, null, null, null, null, null, null, null, null},
            },
            {
                    {w, v, v, v, v, v, v, v, v, v, v, u},
                    {z, y, y, y, y, y, y, y, y, y, y, x},
                    {z, y, null, null, null, null, null, null, null, null, y, x},
                    {z, y, y, y, y, y, y, y, y, y, y, x},
                    {ac, ab, ab, ab, ab, ab, ab, ab, ab, ab, ab, aa},
            },
            {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, h, h, h, h, h, h, h, h, h, h, null},
                {null, h, h, h, h, h, h, h, h, h, h, null},
                {null, h, h, h, h, h, h, h, h, h, h, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
            },
            {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, h, null, h, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, r, null, null, null},
                {null, h, null, h, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
            },
            {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, h, null, h, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, r, null, null, null},
                {null, h, null, h, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
            },
            {
                {null, null, null, null, null, null, b, b, b, b, b, null},
                {null, h, null, h, null, null, b, r, null, r, b, null},
                {null, null, null, null, null, null, b, null, r, null, b, null},
                {null, h, null, h, null, null, b, r, null, r, b, null},
                {null, null, null, null, null, null, b, b, b, b, b, null},
            },
            {
                {null, null, null, null, null, null, b, b, b, b, b, null},
                {null, h, s, h, null, null, b, r, null, r, b, null},
                {null, t, j, h, null, null, b, null, r, null, b, null},
                {null, h, h, h, null, null, b, r, null, r, b, null},
                {null, null, null, null, null, null, b, b, b, b, b, null},
            },
            {
                {null, null, null, null, null, null, q, q, q, q, q, null},
                {null, h, o, h, null, null, q, r, null, r, q, null},
                {null, o, null, o, null, null, q, null, null, null, q, null},
                {null, h, o, h, null, null, q, r, null, r, q, null},
                {null, null, null, null, null, null, q, q, q, q, q, null},
            },
            {
                {null, null, null, null, null, null, b, b, b, b, b, null},
                {null, h, o, h, null, null, b, null, null, null, b, null},
                {null, o, null, o, null, null, b, null, null, null, b, null},
                {null, h, p, h, null, null, b, null, null, null, b, null},
                {null, null, null, null, null, null, b, b, b, b, b, null},
            },
            {
                {e, d, d, d, c, null, b, b, b, b, b, null},
                {i, h, h, h, g, null, b, b, b, b, f, null},
                {i, h, j, h, g, null, b, b, j, b, b, null},
                {i, h, h, h, g, null, b, b, b, b, k, null},
                {n, m, m, m, l, null, b, b, b, b, b, null},
            },
        };

        return new MultiblockPattern(blockArray);
    }



    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biotech." + MachineRegistries.MIXER.id());
    }
}