package com.nstut.biotech.gametest;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.NetTrapBlock;
import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidOutputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.machines.FermenterBlockEntity;
import com.nstut.biotech.blocks.entites.machines.GreenhouseBlockEntity;
import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.AnimalRecipeStatePreparation;
import com.nstut.biotech.recipes.BreedingChamberRecipe;
import com.nstut.biotech.recipes.GreenhouseRecipe;
import com.nstut.biotech.recipes.SlaughterhouseRecipe;
import com.nstut.biotech.recipes.TerrestrialHabitatRecipe;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class BiotechGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, Biotech.MOD_ID);
    private static final int MAX_TICKS = 100;
    private static final int EXPECTED_BIOTECH_RECIPE_COUNT = 80;

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HATCHES_ENFORCE_EXTERNAL_IO_DIRECTION = register("hatches_enforce_external_io_direction", BiotechGameTests::hatchesEnforceExternalIoDirection);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> INVALID_STRUCTURE_AND_RELOAD_PRESERVE_ACTIVE_MACHINE_TRANSACTION = register("invalid_structure_and_reload_preserve_active_machine_transaction", BiotechGameTests::invalidStructureAndReloadPreserveActiveMachineTransaction);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RECIPE_TYPES_ARE_REGISTRY_BACKED = register("recipe_types_are_registry_backed", BiotechGameTests::recipeTypesAreRegistryBacked);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GREENHOUSE_BEETROOT_RECIPE_LOADS_AND_PROCESSES = register("greenhouse_beetroot_recipe_loads_and_processes", BiotechGameTests::greenhouseBeetrootRecipeLoadsAndProcesses);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CAPTURED_ANIMAL_STORAGE_SANITIZES_BEFORE_PERSISTING = register("captured_animal_storage_sanitizes_before_persisting", BiotechGameTests::capturedAnimalStorageSanitizesBeforePersisting);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CAPTURE_ELIGIBILITY_REJECTS_TAGGED_NON_CREATABLE_TYPES = register("capture_eligibility_rejects_tagged_non_creatable_types", BiotechGameTests::captureEligibilityRejectsTaggedNonCreatableTypes);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GENERIC_CAPTURED_SPECIES_RECIPE_MATCHES_LIFECYCLE_SELECTORS = register("generic_captured_species_recipe_matches_lifecycle_selectors", BiotechGameTests::genericCapturedSpeciesRecipeMatchesLifecycleSelectors);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ANIMAL_MACHINE_PREPARATION_PRESERVES_AND_INHERITS_STATE = register("animal_machine_preparation_preserves_and_inherits_state", BiotechGameTests::animalMachinePreparationPreservesAndInheritsState);

    private BiotechGameTests() {}

    private static DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> register(String name, Consumer<GameTestHelper> test) {
        return TEST_FUNCTIONS.register(name, () -> test);
    }

    public static void register(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "hardening"));
        Identifier emptyStructure = Identifier.withDefaultNamespace("empty");
        for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test : List.of(
                HATCHES_ENFORCE_EXTERNAL_IO_DIRECTION,
                INVALID_STRUCTURE_AND_RELOAD_PRESERVE_ACTIVE_MACHINE_TRANSACTION,
                RECIPE_TYPES_ARE_REGISTRY_BACKED,
                GREENHOUSE_BEETROOT_RECIPE_LOADS_AND_PROCESSES,
                CAPTURED_ANIMAL_STORAGE_SANITIZES_BEFORE_PERSISTING,
                CAPTURE_ELIGIBILITY_REJECTS_TAGGED_NON_CREATABLE_TYPES,
                GENERIC_CAPTURED_SPECIES_RECIPE_MATCHES_LIFECYCLE_SELECTORS,
                ANIMAL_MACHINE_PREPARATION_PRESERVES_AND_INHERITS_STATE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.getKey(), new TestData<>(environment, emptyStructure, MAX_TICKS, 0, true)));
        }
    }

    private static void hatchesEnforceExternalIoDirection(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockPos absolutePos = helper.absolutePos(pos);
        Direction externalSide = Direction.NORTH;

        helper.setBlock(pos, BlockRegistries.ITEM_INPUT_HATCH.get().defaultBlockState().setValue(IOHatchBlock.FACING, externalSide));
        var itemInputNative = helper.getLevel().getCapability(Capabilities.Item.BLOCK, absolutePos, externalSide);
        helper.assertTrue(itemInputNative != null, "Item input hatch must expose its external item capability");
        IItemHandler itemInputExternal = IItemHandler.of(itemInputNative);
        helper.assertTrue(itemInputExternal.insertItem(0, new ItemStack(Items.COBBLESTONE), false).isEmpty(), "Item input hatch must accept external insertion on its facing side");
        helper.assertTrue(itemInputExternal.extractItem(0, 1, false).isEmpty(), "Item input hatch must reject external extraction");

        helper.setBlock(pos, BlockRegistries.ITEM_OUTPUT_HATCH.get().defaultBlockState().setValue(IOHatchBlock.FACING, externalSide));
        ItemOutputHatchBlockEntity itemOutput = helper.getBlockEntity(pos, ItemOutputHatchBlockEntity.class);
        helper.assertTrue(itemOutput.getInternalItemStorage().insertItem(0, new ItemStack(Items.IRON_INGOT), false).isEmpty(), "Test setup must populate item output hatch");
        var itemOutputNative = helper.getLevel().getCapability(Capabilities.Item.BLOCK, absolutePos, externalSide);
        helper.assertTrue(itemOutputNative != null, "Item output hatch must expose its external item capability");
        IItemHandler itemOutputExternal = IItemHandler.of(itemOutputNative);
        helper.assertTrue(itemOutputExternal.insertItem(0, new ItemStack(Items.GOLD_INGOT), false).getCount() == 1, "Item output hatch must reject external insertion");
        helper.assertTrue(itemOutputExternal.extractItem(0, 1, false).is(Items.IRON_INGOT), "Item output hatch must permit external extraction");

        helper.setBlock(pos, BlockRegistries.FLUID_INPUT_HATCH.get().defaultBlockState().setValue(IOHatchBlock.FACING, externalSide));
        var fluidInputNative = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, absolutePos, externalSide);
        helper.assertTrue(fluidInputNative != null, "Fluid input hatch must expose its external fluid capability");
        IFluidHandler fluidInputExternal = IFluidHandler.of(fluidInputNative);
        FluidStack waterBucket = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
        helper.assertTrue(fluidInputExternal.fill(waterBucket, IFluidHandler.FluidAction.EXECUTE) == FluidType.BUCKET_VOLUME, "Fluid input hatch must accept external fill");
        helper.assertTrue(fluidInputExternal.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "Fluid input hatch must reject external drain");

        helper.setBlock(pos, BlockRegistries.FLUID_OUTPUT_HATCH.get().defaultBlockState().setValue(IOHatchBlock.FACING, externalSide));
        FluidOutputHatchBlockEntity fluidOutput = helper.getBlockEntity(pos, FluidOutputHatchBlockEntity.class);
        fluidOutput.getInternalTank().fill(waterBucket, IFluidHandler.FluidAction.EXECUTE);
        var fluidOutputNative = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, absolutePos, externalSide);
        helper.assertTrue(fluidOutputNative != null, "Fluid output hatch must expose its external fluid capability");
        IFluidHandler fluidOutputExternal = IFluidHandler.of(fluidOutputNative);
        helper.assertTrue(fluidOutputExternal.fill(waterBucket, IFluidHandler.FluidAction.EXECUTE) == 0, "Fluid output hatch must reject external fill");
        helper.assertTrue(fluidOutputExternal.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).getAmount() == FluidType.BUCKET_VOLUME, "Fluid output hatch must permit external drain");

        helper.setBlock(pos, BlockRegistries.ENERGY_INPUT_HATCH.get().defaultBlockState().setValue(IOHatchBlock.FACING, externalSide));
        var externalEnergy = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, absolutePos, externalSide);
        helper.assertTrue(externalEnergy != null, "Energy input hatch must expose its external energy capability");
        try (var transaction = Transaction.openRoot()) {
            helper.assertTrue(externalEnergy.insert(128, transaction) == 128, "Energy input hatch must accept external FE");
            transaction.commit();
        }
        try (var transaction = Transaction.openRoot()) {
            helper.assertTrue(externalEnergy.extract(128, transaction) == 0, "Energy input hatch must reject external FE extraction");
        }
        helper.succeed();
    }

    private static void invalidStructureAndReloadPreserveActiveMachineTransaction(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockState controllerState = MachineRegistries.FERMENTER.block().get().defaultBlockState();
        helper.setBlock(relativePos, controllerState);
        FermenterBlockEntity machine = helper.getBlockEntity(relativePos, FermenterBlockEntity.class);

        Identifier recipeId = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_active_recipe");
        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);
        int[] outputRolls = {0, 2};
        setMachineField(machine, "activeRecipeKey", recipeKey);
        setMachineField(machine, "activeItemOutputIndexes", outputRolls);
        setMachineField(machine, "energyConsumed", 77);
        setMachineField(machine, "recipeEnergyCost", 200);
        setMachineField(machine, "ingredientsConsumed", true);

        MachineBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), controllerState, machine);

        var registries = helper.getLevel().registryAccess();
        CompoundTag saved = machine.saveWithFullMetadata(registries);
        helper.assertTrue(saved.getString("activeRecipeId").orElse("").equals(recipeId.toString()), "Breaking/invalidating the structure must not discard the active recipe");
        helper.assertTrue(saved.getInt("energyConsumed").orElse(0) == 77 && saved.getBooleanOr("ingredientsConsumed", false), "Invalid structure tick must preserve transaction progress and consumed-input state");
        helper.assertTrue(Arrays.equals(saved.getIntArray("activeItemOutputIndexes").orElseGet(() -> new int[0]), outputRolls), "Persisted chance-output decisions must survive structure invalidation");

        FermenterBlockEntity reloaded = new FermenterBlockEntity(machine.getBlockPos(), controllerState);
        reloaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, saved));
        helper.assertTrue(recipeKey.equals(getMachineField(reloaded, "activeRecipeKey")), "Reload must restore active recipe identity");
        helper.assertTrue((int) getMachineField(reloaded, "energyConsumed") == 77, "Reload must restore exact machine progress");
        helper.assertTrue((boolean) getMachineField(reloaded, "ingredientsConsumed"), "Reload must preserve one-time input consumption state");
        helper.assertTrue(Arrays.equals((int[]) getMachineField(reloaded, "activeItemOutputIndexes"), outputRolls), "Reload must reuse the same probabilistic output decisions");
        helper.succeed();
    }

    private static void capturedAnimalStorageSanitizesBeforePersisting(GameTestHelper helper) {
        CompoundTag source = new CompoundTag();
        source.putString("CustomName", "Bessie");
        source.putInt("Age", -1200);
        source.putInt("UUID", 1);
        source.putInt("Pos", 1);
        source.putInt("Motion", 1);
        source.putInt("Rotation", 1);
        source.putInt("Leash", 1);

        ItemStack captured = new ItemStack(Items.PAPER);
        CapturedAnimalStackState.writeCapture(captured, source, "minecraft:cow", -1);

        CompoundTag root = captured.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag stored = root.getCompound(com.nstut.biotech.blocks.NetTrapBlock.CAPTURED_ENTITY_TAG).orElseThrow();
        helper.assertTrue(stored.getString("CustomName").orElse("").equals("Bessie")
                        && stored.getInt("Age").orElse(0) == -1200,
                "Persistent entity state must survive capture storage");
        helper.assertTrue(!stored.contains("UUID") && !stored.contains("Pos")
                        && !stored.contains("Motion") && !stored.contains("Rotation") && !stored.contains("Leash"),
                "Transient world identity must be removed before captured NBT is persisted");
        helper.assertTrue(root.getString(CapturedAnimalItem.ENTITY_TYPE_TAG).orElse("").equals("minecraft:cow"),
                "Captured entity type metadata must survive alongside sanitized state");
        helper.assertTrue(stored.equals(CapturedAnimalStackState.read(captured)),
                "Captured state must round-trip from raw item storage through the production reader");
        helper.assertTrue(source.contains("UUID") && source.contains("Pos"),
                "Capture storage must not mutate the source entity NBT");
        helper.succeed();
    }
    private static void captureEligibilityRejectsTaggedNonCreatableTypes(GameTestHelper helper) {
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

    private static void recipeTypesAreRegistryBacked(GameTestHelper helper) {
        assertRecipeTypeRegistered(helper, MachineRegistries.BREEDING_CHAMBER.recipeType().get(), "breeding_chamber");
        assertRecipeTypeRegistered(helper, MachineRegistries.TERRESTRIAL_HABITAT.recipeType().get(), "terrestrial_habitat");
        assertRecipeTypeRegistered(helper, MachineRegistries.SLAUGHTERHOUSE.recipeType().get(), "slaughterhouse");
        assertRecipeTypeRegistered(helper, MachineRegistries.GREENHOUSE.recipeType().get(), "greenhouse");
        assertRecipeTypeRegistered(helper, MachineRegistries.FERMENTER.recipeType().get(), "fermenter");
        assertRecipeTypeRegistered(helper, MachineRegistries.MIXER.recipeType().get(), "mixer");
        helper.succeed();
    }

    private static void greenhouseBeetrootRecipeLoadsAndProcesses(GameTestHelper helper) {
        Identifier recipeId = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "greenhouse_beetroot");
        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);
        var loaded = helper.getLevel().recipeAccess().byKey(recipeKey);
        helper.assertTrue(loaded.isPresent(), "greenhouse_beetroot must be present in the live recipe manager");
        helper.assertTrue(loaded.get().value() instanceof GreenhouseRecipe, "greenhouse_beetroot must decode as a GreenhouseRecipe");
        GreenhouseRecipe recipe = (GreenhouseRecipe) loaded.get().value();

        long biotechRecipeCount = helper.getLevel().recipeAccess().getRecipes().stream()
                .filter(holder -> Biotech.MOD_ID.equals(holder.id().identifier().getNamespace()))
                .count();
        helper.assertTrue(biotechRecipeCount == EXPECTED_BIOTECH_RECIPE_COUNT,
                "Expected " + EXPECTED_BIOTECH_RECIPE_COUNT + " loaded Biotech recipes, got " + biotechRecipeCount);
        helper.assertTrue(recipe.getTotalEnergy() == 128000, "greenhouse_beetroot must retain its configured 128000 FE cost");

        BlockPos controllerRelative = new BlockPos(8, 1, 8);
        GreenhouseBlockEntity machine = placeGreenhouseStructure(helper, controllerRelative);
        BlockPos controllerPos = machine.getBlockPos();

        ItemInputHatchBlockEntity itemInput = requireBlockEntity(helper, controllerPos.offset(-3, 0, -3), ItemInputHatchBlockEntity.class);
        ItemOutputHatchBlockEntity itemOutput = requireBlockEntity(helper, controllerPos.offset(3, 0, -3), ItemOutputHatchBlockEntity.class);
        EnergyInputHatchBlockEntity energyInput = requireBlockEntity(helper, controllerPos.offset(0, 0, -6), EnergyInputHatchBlockEntity.class);
        FluidInputHatchBlockEntity fluidInput = requireBlockEntity(helper, controllerPos.offset(-2, 0, -6), FluidInputHatchBlockEntity.class);

        itemInput.getInternalItemStorage().setStackInSlot(0, new ItemStack(Items.BEETROOT_SEEDS, 2));
        fluidInput.setFluid(new FluidStack(Fluids.WATER, 400));
        energyInput.setEnergy(recipe.getTotalEnergy());

        helper.assertTrue(recipe.recipeMatch(
                        itemInput.getInternalItemStorage(),
                        List.of(fluidInput.getInternalTank()),
                        itemOutput.getInternalItemStorage(),
                        List.of()),
                "Loaded greenhouse_beetroot recipe must match two beetroot seeds plus 400 mB water");

        for (int tick = 0; tick < 260 && countItem(itemOutput.getInternalItemStorage(), Items.BEETROOT) == 0; tick++) {
            BlockState state = helper.getLevel().getBlockState(controllerPos);
            MachineBlockEntity.serverTick(helper.getLevel(), controllerPos, state, machine);
        }

        helper.assertTrue(itemInput.getInternalItemStorage().getStackInSlot(0).isEmpty(), "Greenhouse must consume the two beetroot seeds exactly once");
        helper.assertTrue(fluidInput.getInternalTank().getFluidInTank(0).isEmpty(), "Greenhouse must consume the configured 400 mB water");
        helper.assertTrue(energyInput.getInternalEnergyStorage().getEnergyStored() == 0, "Greenhouse must consume the configured 128000 FE");
        helper.assertTrue(countItem(itemOutput.getInternalItemStorage(), Items.BEETROOT) == 4, "Greenhouse must produce four beetroot");
        helper.assertTrue(countItem(itemOutput.getInternalItemStorage(), Items.BEETROOT_SEEDS) == 6, "Greenhouse must produce six beetroot seeds");
        helper.succeed();
    }

    private static void animalMachinePreparationPreservesAndInheritsState(GameTestHelper helper) {
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
                Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_stateful_breeding"),
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
        helper.assertTrue(newbornState.getInt("Age").orElse(0) == -24000,
                "Prepared breeding output must be a newborn");
        helper.assertTrue(newbornState.getByte("Color").orElse((byte) 0) == 14,
                "Offspring inheritance must deterministically use the first matching parent");
        helper.assertTrue(!newbornState.contains("CustomName"),
                "A newborn must not inherit individual identity such as the parent's custom name");
        helper.assertTrue("Parent A".equals(CapturedAnimalStackState.read(breedingInputs.getStackInSlot(0)).getString("CustomName").orElse("")),
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
                Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_stateful_growth"),
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
        helper.assertTrue(adultState.getInt("Age").orElse(-1) == 0 && adultState.getInt("ForcedAge").orElse(-1) == 0,
                "Habitat growth must finish ageing the same captured individual");
        helper.assertTrue(adultState.getByte("Color").orElse((byte) 0) == 11
                        && "Lamb".equals(adultState.getString("CustomName").orElse("")),
                "Habitat growth must preserve variant and individual gameplay state");
        helper.succeed();
    }

    private static void genericCapturedSpeciesRecipeMatchesLifecycleSelectors(GameTestHelper helper) {
        BreedingChamberRecipe breeding = new BreedingChamberRecipe(
                Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_generic_horse_adult"),
                genericSpeciesRecipeData(genericRequirement(EntityType.HORSE, CapturedAnimalItem.LIFECYCLE_ADULT)));
        TerrestrialHabitatRecipe habitat = new TerrestrialHabitatRecipe(
                Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_generic_horse_baby"),
                genericSpeciesRecipeData(genericRequirement(EntityType.HORSE, CapturedAnimalItem.LIFECYCLE_BABY)));
        SlaughterhouseRecipe slaughter = new SlaughterhouseRecipe(
                Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "gametest_generic_horse_any"),
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

    private static GreenhouseBlockEntity placeGreenhouseStructure(GameTestHelper helper, BlockPos controllerRelative) {
        BlockPos controllerPos = helper.absolutePos(controllerRelative);
        BlockState controllerState = MachineRegistries.GREENHOUSE.block().get().defaultBlockState()
                .setValue(MachineBlock.FACING, Direction.SOUTH);
        GreenhouseBlockEntity blueprint = new GreenhouseBlockEntity(controllerPos, controllerState);
        MultiblockPattern pattern = blueprint.getMultiblockPattern();
        MultiblockBlock[][][] blocks = pattern.getPattern();

        for (int y = 0; y < blocks.length; y++) {
            int patternY = blocks.length - 1 - y;
            MultiblockBlock[][] layer = blocks[patternY];
            for (int z = 0; z < layer.length; z++) {
                for (int x = 0; x < layer[z].length; x++) {
                    MultiblockBlock expected = layer[z][x];
                    if (expected == null) continue;
                    BlockPos target = MultiblockPattern.rotateBlockPos(
                            controllerPos,
                            blueprint.getSouthOffsetX(),
                            blueprint.getSouthOffsetY(),
                            blueprint.getSouthOffsetZ(),
                            blocks.length,
                            layer.length,
                            x,
                            patternY,
                            z,
                            controllerState);
                    BlockState state = applyProperties(expected.getBlock().defaultBlockState(), expected.getStates());
                    helper.getLevel().setBlock(target, state, 3);
                }
            }
        }

        GreenhouseBlockEntity machine = requireBlockEntity(helper, controllerPos, GreenhouseBlockEntity.class);
        helper.assertTrue(machine.checkMultiblock(helper.getLevel(), controllerPos, helper.getLevel().getBlockState(controllerPos)),
                "GameTest greenhouse structure must be valid before recipe processing");
        return machine;
    }

    private static BlockState applyProperties(BlockState state, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null) throw new AssertionError("Missing block-state property " + entry.getKey() + " on " + state.getBlock());
            state = applyProperty(state, property, entry.getValue());
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> property, String value) {
        T parsed = property.getValue(value)
                .orElseThrow(() -> new AssertionError("Invalid value " + value + " for property " + property.getName()));
        return state.setValue(property, parsed);
    }

    private static <T extends BlockEntity> T requireBlockEntity(GameTestHelper helper, BlockPos absolutePos, Class<T> type) {
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolutePos);
        if (!type.isInstance(blockEntity)) {
            throw new AssertionError("Expected " + type.getSimpleName() + " at " + absolutePos + ", got " + blockEntity);
        }
        return type.cast(blockEntity);
    }

    private static int countItem(IItemHandler handler, Item item) {
        int count = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static void assertRecipeTypeRegistered(GameTestHelper helper, RecipeType<?> type, String path) {
        Identifier actualId = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        Identifier expectedId = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, path);
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
