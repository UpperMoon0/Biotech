package com.nstut.biotech.gametest;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.NetTrapBlock;
import com.nstut.biotech.blocks.entites.hatches.FluidOutputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.machines.FermenterBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.AnimalRecipeStatePreparation;
import com.nstut.biotech.recipes.BreedingChamberRecipe;
import com.nstut.biotech.recipes.SlaughterhouseRecipe;
import com.nstut.biotech.recipes.TerrestrialHabitatRecipe;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import com.nstut.biotech.items.AnimalItemPreviewPresentation;
import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
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
import net.neoforged.neoforge.items.ItemStackHandler;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@GameTestHolder(Biotech.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BiotechGameTests {
    private static final String TEST_TEMPLATE_NAMESPACE = Biotech.MOD_ID;
    private static final String EMPTY_TEMPLATE = "empty";

    private BiotechGameTests() {
    }

    @GameTest(templateNamespace = TEST_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
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

    @GameTest(templateNamespace = TEST_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
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

    @GameTest(templateNamespace = TEST_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void recipeTypesAreRegistryBacked(GameTestHelper helper) {
        assertRecipeTypeRegistered(helper, MachineRegistries.BREEDING_CHAMBER.recipeType().get(), "breeding_chamber");
        assertRecipeTypeRegistered(helper, MachineRegistries.TERRESTRIAL_HABITAT.recipeType().get(), "terrestrial_habitat");
        assertRecipeTypeRegistered(helper, MachineRegistries.SLAUGHTERHOUSE.recipeType().get(), "slaughterhouse");
        assertRecipeTypeRegistered(helper, MachineRegistries.GREENHOUSE.recipeType().get(), "greenhouse");
        assertRecipeTypeRegistered(helper, MachineRegistries.FERMENTER.recipeType().get(), "fermenter");
        assertRecipeTypeRegistered(helper, MachineRegistries.MIXER.recipeType().get(), "mixer");
        helper.succeed();
    }


    @GameTest(templateNamespace = TEST_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void previewPresentationSuppressesWorldEffects(GameTestHelper helper) {
        Entity entity = EntityType.COW.create(helper.getLevel());
        helper.assertTrue(entity != null, "Test setup must create a cow");
        entity.setCustomName(Component.literal("Bessie"));
        entity.setCustomNameVisible(true);
        entity.setRemainingFireTicks(200);
        entity.setGlowingTag(true);

        helper.assertTrue(entity.isCustomNameVisible() && entity.getRemainingFireTicks() > 0 && entity.hasGlowingTag(),
                "Test setup must enable world-only presentation state");
        AnimalItemPreviewPresentation.suppressWorldPresentation(entity);

        helper.assertTrue(!entity.isCustomNameVisible(), "Animal item preview must not render a world nametag");
        helper.assertTrue(entity.getRemainingFireTicks() == 0, "Animal item preview must not render entity flames");
        helper.assertTrue(!entity.hasGlowingTag(), "Animal item preview must not render a world glowing outline");
        helper.assertTrue(entity.getCustomName() != null && "Bessie".equals(entity.getCustomName().getString()),
                "Suppressing preview presentation must not erase the captured custom name itself");
        helper.succeed();
    }
    @GameTest(templateNamespace = TEST_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void captureEligibilityRejectsTaggedNonCreatableTypes(GameTestHelper helper) {
        helper.assertTrue(!NetTrapBlock.isCaptureTypeSupported(EntityType.PLAYER, true, helper.getLevel()),
                "A datapack-tagged player must be rejected before capture because it cannot be reconstructed");
        helper.assertTrue(NetTrapBlock.isCaptureTypeSupported(EntityType.ARMOR_STAND, true, helper.getLevel()),
                "A tagged constructible non-animal must remain supported by the generic datapack contract");
        helper.assertTrue(NetTrapBlock.isCaptureTypeSupported(EntityType.COW, true, helper.getLevel()),
                "A tagged, reconstructible animal must remain capturable");
        helper.assertTrue(!NetTrapBlock.isCaptureTypeSupported(EntityType.COW, false, helper.getLevel()),
                "A reconstructible animal still requires datapack tag membership");
        helper.succeed();
    }

    @GameTest(templateNamespace = TEST_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void animalMachinePreparationPreservesAndInheritsState(GameTestHelper helper) {
        ItemStack adultParent = new ItemStack(ItemRegistries.SHEEP.get());
        CompoundTag parentState = new CompoundTag();
        parentState.putInt("Age", 0);
        parentState.putByte("Color", (byte) 14);
        parentState.putString("CustomName", "Parent A");
        CapturedAnimalStackState.writeCapture(adultParent, parentState, "minecraft:sheep", 14);

        ItemStack otherParent = new ItemStack(ItemRegistries.SHEEP.get());
        CompoundTag otherState = new CompoundTag();
        otherState.putInt("Age", 0);
        otherState.putByte("Color", (byte) 3);
        otherState.putString("CustomName", "Parent B");
        CapturedAnimalStackState.writeCapture(otherParent, otherState, "minecraft:sheep", 3);

        ItemStackHandler breedingInputs = new ItemStackHandler(2);
        breedingInputs.setStackInSlot(0, adultParent.copy());
        breedingInputs.setStackInSlot(1, otherParent.copy());
        BreedingChamberRecipe breeding = new BreedingChamberRecipe(
                ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_stateful_breeding"),
                new ModRecipeData(
                        new IngredientItem[] {new IngredientItem(new ItemStack(ItemRegistries.SHEEP.get(), 2), false)},
                        new OutputItem[] {new OutputItem(new ItemStack(ItemRegistries.BABY_SHEEP.get()), 1.0f)},
                        new FluidStack[0],
                        new FluidStack[0],
                        0));

        BreedingChamberRecipe preparedBreeding =
                AnimalRecipeStatePreparation.prepareBreeding(breeding, breedingInputs);
        ItemStack newborn = preparedBreeding.getItemOutputs().get(0).getItemStack();
        CompoundTag newbornState = CapturedAnimalStackState.read(newborn);
        helper.assertTrue(newbornState.getInt("Age") == -24000,
                "Prepared breeding output must be a newborn");
        helper.assertTrue(newbornState.getByte("Color") == 14,
                "Offspring inheritance must deterministically use the first matching parent");
        helper.assertTrue(!newbornState.contains("CustomName"),
                "A newborn must not inherit individual identity such as the parent's custom name");
        helper.assertTrue("Parent A".equals(CapturedAnimalStackState.read(breedingInputs.getStackInSlot(0)).getString("CustomName")),
                "Preparing a breeding transaction must not mutate its parent input");

        ItemStack baby = new ItemStack(ItemRegistries.BABY_SHEEP.get());
        CompoundTag babyState = new CompoundTag();
        babyState.putInt("Age", -1200);
        babyState.putByte("Color", (byte) 11);
        babyState.putString("CustomName", "Lamb");
        CapturedAnimalStackState.writeCapture(baby, babyState, "minecraft:sheep", 11);
        ItemStackHandler growthInputs = new ItemStackHandler(1);
        growthInputs.setStackInSlot(0, baby);

        TerrestrialHabitatRecipe growth = new TerrestrialHabitatRecipe(
                ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_stateful_growth"),
                new ModRecipeData(
                        new IngredientItem[] {new IngredientItem(new ItemStack(ItemRegistries.BABY_SHEEP.get()), true)},
                        new OutputItem[] {new OutputItem(new ItemStack(ItemRegistries.SHEEP.get()), 1.0f)},
                        new FluidStack[0],
                        new FluidStack[0],
                        0));
        TerrestrialHabitatRecipe preparedGrowth =
                AnimalRecipeStatePreparation.prepareGrowth(growth, growthInputs);
        ItemStack adult = preparedGrowth.getItemOutputs().get(0).getItemStack();
        CompoundTag adultState = CapturedAnimalStackState.read(adult);
        helper.assertTrue(adultState.getInt("Age") == 0 && adultState.getInt("ForcedAge") == 0,
                "Habitat growth must finish ageing the same captured individual");
        helper.assertTrue(adultState.getByte("Color") == 11 && "Lamb".equals(adultState.getString("CustomName")),
                "Habitat growth must preserve variant and individual gameplay state");
        helper.succeed();
    }

    @GameTest(templateNamespace = TEST_TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = 100)
    public static void genericCapturedSpeciesRecipeMatchesLifecycleSelectors(GameTestHelper helper) {
        BreedingChamberRecipe breeding = new BreedingChamberRecipe(
                ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_generic_horse_adult"),
                genericSpeciesRecipeData(genericRequirement(EntityType.HORSE, CapturedAnimalItem.LIFECYCLE_ADULT)));
        TerrestrialHabitatRecipe habitat = new TerrestrialHabitatRecipe(
                ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_generic_horse_baby"),
                genericSpeciesRecipeData(genericRequirement(EntityType.HORSE, CapturedAnimalItem.LIFECYCLE_BABY)));
        SlaughterhouseRecipe slaughter = new SlaughterhouseRecipe(
                ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_generic_horse_any"),
                genericSpeciesRecipeData(genericRequirement(EntityType.HORSE, CapturedAnimalItem.LIFECYCLE_ANY)));

        ItemStack adultHorse = capturedGeneric(EntityType.HORSE, "Adult Horse", 0);
        ItemStack babyHorse = capturedGeneric(EntityType.HORSE, "Baby Horse", -1200);
        ItemStack goat = capturedGeneric(EntityType.GOAT, "Wrong Species", 0);
        ItemStackHandler inputs = new ItemStackHandler(1);

        inputs.setStackInSlot(0, adultHorse);
        helper.assertTrue(breeding.recipeMatch(inputs, List.of(), null, List.of()),
                "Breeding Chamber generic adult selector must accept an adult horse");
        inputs.setStackInSlot(0, babyHorse);
        helper.assertTrue(!breeding.recipeMatch(inputs, List.of(), null, List.of()),
                "Breeding Chamber generic adult selector must reject a baby horse");

        helper.assertTrue(habitat.recipeMatch(inputs, List.of(), null, List.of()),
                "Terrestrial Habitat generic baby selector must accept a baby horse");
        inputs.setStackInSlot(0, adultHorse);
        helper.assertTrue(!habitat.recipeMatch(inputs, List.of(), null, List.of()),
                "Terrestrial Habitat generic baby selector must reject an adult horse");

        helper.assertTrue(slaughter.recipeMatch(inputs, List.of(), null, List.of()),
                "Slaughterhouse generic any selector must accept an adult horse");
        inputs.setStackInSlot(0, babyHorse);
        helper.assertTrue(slaughter.recipeMatch(inputs, List.of(), null, List.of()),
                "Slaughterhouse generic any selector must also accept a baby horse");
        inputs.setStackInSlot(0, goat);
        helper.assertTrue(!slaughter.recipeMatch(inputs, List.of(), null, List.of()),
                "Generic horse requirements must still reject another species");
        helper.succeed();
    }

    private static ItemStack genericRequirement(EntityType<?> type, String lifecycle) {
        ItemStack stack = new ItemStack(ItemRegistries.CAPTURED_ANIMAL.get());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            root.putString(CapturedAnimalItem.ENTITY_TYPE_TAG, EntityType.getKey(type).toString());
            root.putString(CapturedAnimalItem.RECIPE_LIFECYCLE_TAG, lifecycle);
        });
        return stack;
    }

    private static ItemStack capturedGeneric(EntityType<?> type, String name, int age) {
        ItemStack stack = new ItemStack(ItemRegistries.CAPTURED_ANIMAL.get());
        CompoundTag state = new CompoundTag();
        state.putString("CustomName", name);
        state.putInt("Age", age);
        CapturedAnimalStackState.writeCapture(stack, state, EntityType.getKey(type).toString(), -1);
        return stack;
    }

    private static ModRecipeData genericSpeciesRecipeData(ItemStack requirement) {
        return new ModRecipeData(
                new IngredientItem[] {new IngredientItem(requirement, true)},
                new OutputItem[0],
                new FluidStack[0],
                new FluidStack[0],
                0);
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
