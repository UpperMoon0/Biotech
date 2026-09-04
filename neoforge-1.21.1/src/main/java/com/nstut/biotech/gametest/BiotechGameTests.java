package com.nstut.biotech.gametest;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.hatches.FluidOutputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.machines.FermenterBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import java.lang.reflect.Field;
import java.util.Arrays;

@GameTestHolder(Biotech.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BiotechGameTests {
    private static final String VANILLA_TEMPLATE_NAMESPACE = "minecraft";
    private static final String EMPTY_TEMPLATE = "empty";

    private BiotechGameTests() {
    }

    @GameTest(templateNamespace = VANILLA_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void hatchesEnforceExternalIoDirection(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockPos absolutePos = helper.absolutePos(pos);
        Direction externalSide = Direction.NORTH;

        BlockState itemInputState = BlockRegistries.ITEM_INPUT_HATCH.get().defaultBlockState()
                .setValue(IOHatchBlock.FACING, externalSide);
        helper.setBlock(pos, itemInputState);
        IItemHandler itemInputExternal = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, absolutePos, externalSide);
        helper.assertTrue(itemInputExternal != null, "Item input hatch must expose its external item capability");
        helper.assertTrue(itemInputExternal.insertItem(0, new ItemStack(Items.COBBLESTONE), false).isEmpty(),
                "Item input hatch must accept external insertion on its facing side");
        helper.assertTrue(itemInputExternal.extractItem(0, 1, false).isEmpty(),
                "Item input hatch must reject external extraction");

        BlockState itemOutputState = BlockRegistries.ITEM_OUTPUT_HATCH.get().defaultBlockState()
                .setValue(IOHatchBlock.FACING, externalSide);
        helper.setBlock(pos, itemOutputState);
        ItemOutputHatchBlockEntity itemOutput = (ItemOutputHatchBlockEntity) helper.getBlockEntity(pos);
        itemOutput.getInternalItemStorage().setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
        IItemHandler itemOutputExternal = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, absolutePos, externalSide);
        helper.assertTrue(itemOutputExternal != null, "Item output hatch must expose its external item capability");
        helper.assertTrue(itemOutputExternal.insertItem(0, new ItemStack(Items.GOLD_INGOT), false).getCount() == 1,
                "Item output hatch must reject external insertion");
        helper.assertTrue(itemOutputExternal.extractItem(0, 1, false).is(Items.IRON_INGOT),
                "Item output hatch must permit external extraction");

        BlockState fluidInputState = BlockRegistries.FLUID_INPUT_HATCH.get().defaultBlockState()
                .setValue(IOHatchBlock.FACING, externalSide);
        helper.setBlock(pos, fluidInputState);
        IFluidHandler fluidInputExternal = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, absolutePos, externalSide);
        helper.assertTrue(fluidInputExternal != null, "Fluid input hatch must expose its external fluid capability");
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
        IFluidHandler fluidOutputExternal = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, absolutePos, externalSide);
        helper.assertTrue(fluidOutputExternal != null, "Fluid output hatch must expose its external fluid capability");
        helper.assertTrue(fluidOutputExternal.fill(waterBucket, IFluidHandler.FluidAction.EXECUTE) == 0,
                "Fluid output hatch must reject external fill");
        helper.assertTrue(fluidOutputExternal.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).getAmount()
                        == FluidType.BUCKET_VOLUME,
                "Fluid output hatch must permit external drain");

        BlockState energyState = BlockRegistries.ENERGY_INPUT_HATCH.get().defaultBlockState()
                .setValue(IOHatchBlock.FACING, externalSide);
        helper.setBlock(pos, energyState);
        IEnergyStorage externalEnergy = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, absolutePos, externalSide);
        helper.assertTrue(externalEnergy != null, "Energy input hatch must expose its external energy capability");
        helper.assertTrue(externalEnergy.receiveEnergy(128, false) == 128,
                "Energy input hatch must accept external FE");
        helper.assertTrue(externalEnergy.extractEnergy(128, false) == 0 && !externalEnergy.canExtract(),
                "Energy input hatch must be externally receive-only");

        helper.succeed();
    }

    @GameTest(templateNamespace = VANILLA_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void invalidStructureAndReloadPreserveActiveMachineTransaction(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockState controllerState = MachineRegistries.FERMENTER.block().get().defaultBlockState();
        helper.setBlock(relativePos, controllerState);
        FermenterBlockEntity machine = (FermenterBlockEntity) helper.getBlockEntity(relativePos);

        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_active_recipe");
        int[] outputRolls = {0, 2};
        setMachineField(machine, "activeRecipeId", recipeId);
        setMachineField(machine, "activeItemOutputIndexes", outputRolls);
        setMachineField(machine, "energyConsumed", 77);
        setMachineField(machine, "recipeEnergyCost", 200);
        setMachineField(machine, "ingredientsConsumed", true);

        MachineBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), controllerState, machine);

        var registries = helper.getLevel().registryAccess();
        CompoundTag saved = machine.saveWithFullMetadata(registries);
        helper.assertTrue(saved.getString("activeRecipeId").equals(recipeId.toString()),
                "Breaking/invalidating the structure must not discard the active recipe");
        helper.assertTrue(saved.getInt("energyConsumed") == 77 && saved.getBoolean("ingredientsConsumed"),
                "Invalid structure tick must preserve transaction progress and consumed-input state");
        helper.assertTrue(Arrays.equals(saved.getIntArray("activeItemOutputIndexes"), outputRolls),
                "Persisted chance-output decisions must survive structure invalidation");

        FermenterBlockEntity reloaded = new FermenterBlockEntity(machine.getBlockPos(), controllerState);
        reloaded.loadWithComponents(saved, registries);
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

    @GameTest(templateNamespace = VANILLA_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void recipeTypesAreRegistryBacked(GameTestHelper helper) {
        assertRecipeTypeRegistered(helper, MachineRegistries.BREEDING_CHAMBER.recipeType().get(), "breeding_chamber");
        assertRecipeTypeRegistered(helper, MachineRegistries.TERRESTRIAL_HABITAT.recipeType().get(), "terrestrial_habitat");
        assertRecipeTypeRegistered(helper, MachineRegistries.SLAUGHTERHOUSE.recipeType().get(), "slaughterhouse");
        assertRecipeTypeRegistered(helper, MachineRegistries.GREENHOUSE.recipeType().get(), "greenhouse");
        assertRecipeTypeRegistered(helper, MachineRegistries.FERMENTER.recipeType().get(), "fermenter");
        assertRecipeTypeRegistered(helper, MachineRegistries.MIXER.recipeType().get(), "mixer");
        helper.succeed();
    }

    private static void assertRecipeTypeRegistered(GameTestHelper helper, RecipeType<?> type, String path) {
        ResourceLocation actualId = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        ResourceLocation expectedId = ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, path);
        helper.assertTrue(expectedId.equals(actualId), "Recipe type must be registered as " + expectedId + ", got " + actualId);
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
