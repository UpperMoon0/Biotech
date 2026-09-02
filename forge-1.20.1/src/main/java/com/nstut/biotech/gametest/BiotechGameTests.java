package com.nstut.biotech.gametest;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidOutputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.machines.FermenterBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Field;
import java.util.Arrays;

@GameTestHolder(Biotech.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BiotechGameTests {
    private static final String FORGE_TEMPLATE_NAMESPACE = "forge";
    private static final String EMPTY_TEMPLATE = "empty3x3x3";

    private BiotechGameTests() {
    }

    @GameTest(templateNamespace = FORGE_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void hatchesEnforceExternalIoDirection(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        Direction externalSide = Direction.NORTH;

        BlockState itemInputState = BlockRegistries.ITEM_INPUT_HATCH.get().defaultBlockState()
                .setValue(IOHatchBlock.FACING, externalSide);
        helper.setBlock(pos, itemInputState);
        ItemInputHatchBlockEntity itemInput = (ItemInputHatchBlockEntity) helper.getBlockEntity(pos);
        IItemHandler itemInputExternal = itemInput.getCapability(ForgeCapabilities.ITEM_HANDLER, externalSide)
                .orElseThrow(IllegalStateException::new);
        helper.assertTrue(itemInputExternal.insertItem(0, new ItemStack(Items.COBBLESTONE), false).isEmpty(),
                "Item input hatch must accept external insertion on its facing side");
        helper.assertTrue(itemInputExternal.extractItem(0, 1, false).isEmpty(),
                "Item input hatch must reject external extraction");

        BlockState itemOutputState = BlockRegistries.ITEM_OUTPUT_HATCH.get().defaultBlockState()
                .setValue(IOHatchBlock.FACING, externalSide);
        helper.setBlock(pos, itemOutputState);
        ItemOutputHatchBlockEntity itemOutput = (ItemOutputHatchBlockEntity) helper.getBlockEntity(pos);
        itemOutput.getInternalItemStorage().setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
        IItemHandler itemOutputExternal = itemOutput.getCapability(ForgeCapabilities.ITEM_HANDLER, externalSide)
                .orElseThrow(IllegalStateException::new);
        helper.assertTrue(itemOutputExternal.insertItem(0, new ItemStack(Items.GOLD_INGOT), false).getCount() == 1,
                "Item output hatch must reject external insertion");
        helper.assertTrue(itemOutputExternal.extractItem(0, 1, false).is(Items.IRON_INGOT),
                "Item output hatch must permit external extraction");

        BlockState fluidInputState = BlockRegistries.FLUID_INPUT_HATCH.get().defaultBlockState()
                .setValue(IOHatchBlock.FACING, externalSide);
        helper.setBlock(pos, fluidInputState);
        FluidInputHatchBlockEntity fluidInput = (FluidInputHatchBlockEntity) helper.getBlockEntity(pos);
        IFluidHandler fluidInputExternal = fluidInput.getCapability(ForgeCapabilities.FLUID_HANDLER, externalSide)
                .orElseThrow(IllegalStateException::new);
        FluidStack waterBucket = new FluidStack(net.minecraft.world.level.material.Fluids.WATER, FluidType.BUCKET_VOLUME);
        helper.assertTrue(fluidInputExternal.fill(waterBucket, IFluidHandler.FluidAction.EXECUTE) == FluidType.BUCKET_VOLUME,
                "Fluid input hatch must accept external fill");
        helper.assertTrue(fluidInputExternal.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).isEmpty(),
                "Fluid input hatch must reject external drain");

        BlockState fluidOutputState = BlockRegistries.FLUID_OUTPUT_HATCH.get().defaultBlockState()
                .setValue(IOHatchBlock.FACING, externalSide);
        helper.setBlock(pos, fluidOutputState);
        FluidOutputHatchBlockEntity fluidOutput = (FluidOutputHatchBlockEntity) helper.getBlockEntity(pos);
        fluidOutput.getInternalTank().fill(waterBucket, IFluidHandler.FluidAction.EXECUTE);
        IFluidHandler fluidOutputExternal = fluidOutput.getCapability(ForgeCapabilities.FLUID_HANDLER, externalSide)
                .orElseThrow(IllegalStateException::new);
        helper.assertTrue(fluidOutputExternal.fill(waterBucket, IFluidHandler.FluidAction.EXECUTE) == 0,
                "Fluid output hatch must reject external fill");
        helper.assertTrue(fluidOutputExternal.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).getAmount()
                        == FluidType.BUCKET_VOLUME,
                "Fluid output hatch must permit external drain");

        BlockState energyState = BlockRegistries.ENERGY_INPUT_HATCH.get().defaultBlockState()
                .setValue(IOHatchBlock.FACING, externalSide);
        helper.setBlock(pos, energyState);
        EnergyInputHatchBlockEntity energyInput = (EnergyInputHatchBlockEntity) helper.getBlockEntity(pos);
        IEnergyStorage externalEnergy = energyInput.getCapability(ForgeCapabilities.ENERGY, externalSide)
                .orElseThrow(IllegalStateException::new);
        helper.assertTrue(externalEnergy.receiveEnergy(128, false) == 128,
                "Energy input hatch must accept external FE");
        helper.assertTrue(externalEnergy.extractEnergy(128, false) == 0 && !externalEnergy.canExtract(),
                "Energy input hatch must be externally receive-only");

        helper.succeed();
    }

    @GameTest(templateNamespace = FORGE_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void invalidStructureAndReloadPreserveActiveMachineTransaction(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockState controllerState = MachineRegistries.FERMENTER.block().get().defaultBlockState();
        helper.setBlock(relativePos, controllerState);
        FermenterBlockEntity machine = (FermenterBlockEntity) helper.getBlockEntity(relativePos);

        ResourceLocation recipeId = new ResourceLocation(Biotech.MOD_ID, "gametest_active_recipe");
        int[] outputRolls = {0, 2};
        setMachineField(machine, "activeRecipeId", recipeId);
        setMachineField(machine, "activeItemOutputIndexes", outputRolls);
        setMachineField(machine, "energyConsumed", 77);
        setMachineField(machine, "recipeEnergyCost", 200);
        setMachineField(machine, "ingredientsConsumed", true);

        MachineBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), controllerState, machine);

        CompoundTag saved = machine.saveWithFullMetadata();
        helper.assertTrue(saved.getString("activeRecipeId").equals(recipeId.toString()),
                "Breaking/invalidating the structure must not discard the active recipe");
        helper.assertTrue(saved.getInt("energyConsumed") == 77 && saved.getBoolean("ingredientsConsumed"),
                "Invalid structure tick must preserve transaction progress and consumed-input state");
        helper.assertTrue(Arrays.equals(saved.getIntArray("activeItemOutputIndexes"), outputRolls),
                "Persisted chance-output decisions must survive structure invalidation");

        FermenterBlockEntity reloaded = new FermenterBlockEntity(machine.getBlockPos(), controllerState);
        reloaded.load(saved);
        helper.assertTrue(recipeId.equals(getMachineField(reloaded, "activeRecipeId")),
                "Reload must restore active recipe identity");
        helper.assertTrue((int) getMachineField(reloaded, "energyConsumed") == 77,
                "Reload must restore exact machine progress");
        helper.assertTrue((boolean) getMachineField(reloaded, "ingredientsConsumed"),
                "Reload must preserve one-time input consumption state");
        helper.assertTrue(Arrays.equals((int[]) getMachineField(reloaded, "activeItemOutputIndexes"), outputRolls),
                "Reload must reuse the same probabilistic output decisions");

        helper.succeed();
    }

    private static void setMachineField(MachineBlockEntity machine, String name, Object value) {
        try {
            Field field = MachineBlockEntity.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(machine, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to set machine test field " + name, exception);
        }
    }

    private static Object getMachineField(MachineBlockEntity machine, String name) {
        try {
            Field field = MachineBlockEntity.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(machine);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to read machine test field " + name, exception);
        }
    }
}
