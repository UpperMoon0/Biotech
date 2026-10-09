package com.nstut.biotech.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Consumer;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.nstut.biotech.Biotech;
import com.nstut.biotech.Config;
import com.nstut.biotech.blocks.entites.hatches.*;
import com.nstut.biotech.blocks.entites.machines.SlaughterhouseBlockEntity;
import com.nstut.biotech.blocks.entites.machines.GreenhouseBlockEntity;
import com.nstut.biotech.blocks.entites.machines.TerrestrialHabitatBlockEntity;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.*;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Regression tests use real structures, hatches, loaded recipes and the production transaction tick. */
public final class LivestockProductionGameTests {
    private LivestockProductionGameTests() {}

    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, Biotech.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ALL_LAND_ANIMALS = TEST_FUNCTIONS.register("all_land_animals", () -> LivestockProductionGameTests::allDefaultLandAnimalsHaveCaptureCreativeAndRecipes);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HORSE_TRAITS = TEST_FUNCTIONS.register("horse_traits", () -> LivestockProductionGameTests::horseAndHybridOffspringKeepVanillaStatsAndParents);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GREENHOUSE_CYCLE = TEST_FUNCTIONS.register("livestock_greenhouse_cycle", () -> LivestockProductionGameTests::greenhouseBlockedReloadKeepsHarvestAndConsumesOnce);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GREENHOUSE_LIVE = TEST_FUNCTIONS.register("livestock_greenhouse_live", () -> LivestockProductionGameTests::greenhouseHarvestsUseLiveMatureLootAndKeepOverrides);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOOTPOLICYSEEDANDSTACKLIMITS = TEST_FUNCTIONS.register("livestock_loot_policy_seed_and_stack_limits", () -> LivestockProductionGameTests::lootPolicySeedAndStackLimits);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SLAUGHTERBLOCKEDPAUSEANDRELOADPRESERVEEXACTLOOT = TEST_FUNCTIONS.register("livestock_slaughter_blocked_pause_and_reload_preserve_exact_loot", () -> LivestockProductionGameTests::slaughterBlockedPauseAndReloadPreserveExactLoot);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BLOCKEDDONORREPLACEMENTANDOVERSIZEARESAFE = TEST_FUNCTIONS.register("livestock_blocked_donor_replacement_and_oversize_are_safe", () -> LivestockProductionGameTests::blockedDonorReplacementAndOversizeAreSafe);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RENEWABLEITEMSREPEATWITHEXACTCOSTS = TEST_FUNCTIONS.register("livestock_renewable_items_repeat_with_exact_costs", () -> LivestockProductionGameTests::renewableItemsRepeatWithExactCosts);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RENEWABLEMILKBLOCKSANDRELOADSWITHOUTCONSUMINGADULT = TEST_FUNCTIONS.register("livestock_renewable_milk_blocks_and_reloads_without_consuming_adult", () -> LivestockProductionGameTests::renewableMilkBlocksAndReloadsWithoutConsumingAdult);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GOATDATAPACKEXAMPLEDECODESANDREPEATS = TEST_FUNCTIONS.register("livestock_goat_datapack_example_decodes_and_repeats", () -> LivestockProductionGameTests::goatDatapackExampleDecodesAndRepeats);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DATAPACKRELOADKEEPSINFLIGHTSLAUGHTER = TEST_FUNCTIONS.register("livestock_datapack_reload_keeps_in_flight_slaughter", () -> LivestockProductionGameTests::datapackReloadKeepsInFlightSlaughter);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTROLLERDIAGNOSTICS = TEST_FUNCTIONS.register("controller_diagnostics_and_redstone", () -> LivestockProductionGameTests::controllerDiagnosticsAndRedstonePreserveCycle);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTROLLERTHROTTLE = TEST_FUNCTIONS.register("controller_diagnostic_throttle_and_empty_recipes", () -> LivestockProductionGameTests::controllerDiagnosticScansAreBoundedAndEmptyRecipesAreDistinct);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTROLLERBALANCE = TEST_FUNCTIONS.register("controller_balance_and_fluid_diagnostics", () -> LivestockProductionGameTests::controllerBalanceSnapshotAndFluidDiagnostics);

    public static void register(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("livestock_production"));
        for (var test : List.of(ALL_LAND_ANIMALS, HORSE_TRAITS, GREENHOUSE_LIVE, GREENHOUSE_CYCLE, CONTROLLERDIAGNOSTICS, CONTROLLERTHROTTLE, CONTROLLERBALANCE, LOOTPOLICYSEEDANDSTACKLIMITS, SLAUGHTERBLOCKEDPAUSEANDRELOADPRESERVEEXACTLOOT, BLOCKEDDONORREPLACEMENTANDOVERSIZEARESAFE, RENEWABLEITEMSREPEATWITHEXACTCOSTS, RENEWABLEMILKBLOCKSANDRELOADSWITHOUTCONSUMINGADULT, GOATDATAPACKEXAMPLEDECODESANDREPEATS, DATAPACKRELOADKEEPSINFLIGHTSLAUGHTER)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.getKey(), new TestData<>(environment, id("livestock"), 400, 0, true)));
        }
    }


    public static void allDefaultLandAnimalsHaveCaptureCreativeAndRecipes(GameTestHelper helper) {
        var creative = com.nstut.biotech.items.DefaultCapturedAnimals.stacks();
        for (var animal : com.nstut.biotech.data.TerrestrialAnimalCatalog.extras(261)) {
            var stack = creative.stream().filter(item -> com.nstut.biotech.items.CapturedAnimalStackState.entityTypeId(item)
                    .equals("minecraft:" + animal.id()) && !com.nstut.biotech.items.CapturedAnimalStackState.read(item).toString().contains("-24000")).findFirst().orElseThrow();
            var entity = ((com.nstut.biotech.items.CapturedAnimalItem) stack.getItem()).createCapturedEntity(helper.getLevel(), stack);
            helper.assertTrue(entity instanceof net.minecraft.world.entity.animal.Animal, "Each default captured land species must reconstruct as a vanilla Animal: " + animal.id());
            helper.assertTrue(com.nstut.biotech.blocks.NetTrapBlock.isCaptureTypeSupported(entity.getType(), entity.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, id("capturable"))), helper.getLevel()),
                    "Every default species must be in the actual capture tag: " + animal.id());
            helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(recipeKey("slaughterhouse_" + animal.id())).isPresent()
                    && helper.getLevel().getServer().getRecipeManager().byKey(recipeKey("terrestrial_habitat_" + animal.id() + "_renewable")).isPresent(),
                    "Default species must have loaded machine recipes: " + animal.id());
            if (animal.breeds()) {
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:" + animal.food()));
                helper.assertTrue(((net.minecraft.world.entity.animal.Animal) entity).isFood(new ItemStack(item)), "Default breeding food must match vanilla: " + animal.id());
            } else helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(recipeKey("breeding_chamber_" + animal.id())).isEmpty(), "Sterile/non-breedable species must not get a fake breeding recipe");
            if (animal.baby()) {
                var baby = creative.stream().filter(item -> com.nstut.biotech.items.CapturedAnimalStackState.entityTypeId(item).equals("minecraft:" + animal.id())
                        && com.nstut.biotech.items.CapturedAnimalStackState.read(item).toString().contains("-24000")).findFirst().orElseThrow();
                helper.assertTrue(((net.minecraft.world.entity.AgeableMob) ((com.nstut.biotech.items.CapturedAnimalItem) baby.getItem()).createCapturedEntity(helper.getLevel(), baby)).isBaby(), "Creative baby variant must actually be a baby: " + animal.id());
            }
            entity.discard();
        }
        helper.succeed();
    }

    public static void horseAndHybridOffspringKeepVanillaStatsAndParents(GameTestHelper helper) {
        var first = statHorse(helper, net.minecraft.world.entity.EntityType.HORSE, 0.15, 0.5, 18);
        var second = statHorse(helper, net.minecraft.world.entity.EntityType.HORSE, 0.30, 0.9, 28);
        var inputs = new net.neoforged.neoforge.items.ItemStackHandler(3);
        inputs.setStackInSlot(0, first.copy()); inputs.setStackInSlot(1, second.copy()); inputs.setStackInSlot(2, new ItemStack(Items.GOLDEN_CARROT, 2));
        var recipe = (BreedingChamberRecipe) helper.getLevel().getServer().getRecipeManager().byKey(recipeKey("breeding_chamber_horse")).orElseThrow().value();
        var prepared = AnimalRecipeStatePreparation.prepareBreeding(recipe, inputs, helper.getLevel());
        encode(helper, prepared.getRecipe());
        var persistenceOps = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var encoded = ModRecipeData.CODEC.encodeStart(persistenceOps, prepared.getRecipe()).result().orElseThrow();
        var restored = ModRecipeData.CODEC.parse(persistenceOps, encoded).result().orElseThrow();
        var newbornStack = restored.getOutputItems()[0].getItemStack();
        var newborn = (net.minecraft.world.entity.animal.equine.AbstractHorse) ((com.nstut.biotech.items.CapturedAnimalItem) newbornStack.getItem()).createCapturedEntity(helper.getLevel(), newbornStack);
        helper.assertTrue(newborn.isBaby() && newborn.getCustomName() == null && !newborn.isTamed(), "Vanilla horse offspring must be a new untamed individual");
        double speed = newborn.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        double jump = newborn.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH);
        helper.assertTrue(speed >= 0.1125 && speed <= 0.3375 && jump >= 0.4 && jump <= 1.0
                        && newborn.getMaxHealth() >= 15 && newborn.getMaxHealth() <= 30 && speed != 0.15,
                "Persisted offspring must keep vanilla mixed speed, jump and health rather than cloning the first parent");
        var lines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        com.nstut.biotech.items.CapturedAnimalTraitTooltip.append(newbornStack, helper.getLevel(), lines::add);
        helper.assertTrue(lines.stream().anyMatch(line -> line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents content
                        && content.getKey().equals("tooltip.biotech.trait.jump")), "Captured horse tooltip must expose its actual jump trait");
        helper.assertTrue(same(first, inputs.getStackInSlot(0)) && same(second, inputs.getStackInSlot(1)), "Preparing offspring must leave both parent's payloads unchanged");
        var donkey = statHorse(helper, net.minecraft.world.entity.EntityType.DONKEY, 0.2, 0.65, 24);
        inputs.setStackInSlot(1, donkey.copy());
        var hybrid = (BreedingChamberRecipe) helper.getLevel().getServer().getRecipeManager().byKey(recipeKey("breeding_chamber_horse_donkey")).orElseThrow().value();
        var mule = AnimalRecipeStatePreparation.prepareBreeding(hybrid, inputs, helper.getLevel()).getItemOutputs().get(0).getItemStack();
        helper.assertTrue(com.nstut.biotech.items.CapturedAnimalStackState.entityTypeId(mule).equals("minecraft:mule")
                        && ((net.minecraft.world.entity.AgeableMob) ((com.nstut.biotech.items.CapturedAnimalItem) mule.getItem()).createCapturedEntity(helper.getLevel(), mule)).isBaby(),
                "Horse plus donkey must produce a baby mule with its own entity identity");
        helper.assertTrue(same(first, inputs.getStackInSlot(0)) && same(donkey, inputs.getStackInSlot(1)), "Hybrid preparation must retain both parents");
        // Wild horses may be captured, but they cannot bypass vanilla taming requirements.
        var wild = statHorse(helper, net.minecraft.world.entity.EntityType.HORSE, 0.2, 0.6, 20);
        var wildState = com.nstut.biotech.items.CapturedAnimalStackState.read(wild); wildState.putBoolean("Tame", false);
        com.nstut.biotech.items.CapturedAnimalStackState.write(wild, wildState); inputs.setStackInSlot(0, wild);
        inputs.setStackInSlot(1, second.copy());
        boolean rejected = false;
        try { AnimalRecipeStatePreparation.prepareBreeding(recipe, inputs, helper.getLevel()); }
        catch (com.nstut.nstutlib.recipes.RecipeTransactionException expected) { rejected = true; }
        helper.assertTrue(rejected && inputs.getStackInSlot(2).getCount() == 2, "Invalid mating must reject before food consumption");
        helper.succeed();
    }

    private static ItemStack statHorse(GameTestHelper helper, net.minecraft.world.entity.EntityType<?> type, double speed, double jump, double health) {
        var entity = (net.minecraft.world.entity.animal.equine.AbstractHorse) type.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE);
        entity.setTamed(true); entity.setCustomName(net.minecraft.network.chat.Component.literal("Parent"));
        entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH).setBaseValue(jump);
        entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(health); entity.setHealth((float) health);
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(output); CompoundTag state = output.buildResult();
        ItemStack stack = new ItemStack(ItemRegistries.CAPTURED_ANIMAL.get());
        com.nstut.biotech.items.CapturedAnimalStackState.writeCapture(stack, state, net.minecraft.world.entity.EntityType.getKey(type).toString(), -1);
        entity.discard(); return stack;
    }

    public static void greenhouseBlockedReloadKeepsHarvestAndConsumesOnce(GameTestHelper helper) {
        var machine = place(helper, 2);
        BlockPos p = machine.getBlockPos();
        var input = block(helper, p.offset(-3, 0, -3), ItemInputHatchBlockEntity.class).getInternalItemStorage();
        var output = block(helper, p.offset(3, 0, -3), ItemOutputHatchBlockEntity.class).getInternalItemStorage();
        var water = block(helper, p.offset(-2, 0, -6), FluidInputHatchBlockEntity.class);
        var energy = block(helper, p.offset(0, 0, -6), EnergyInputHatchBlockEntity.class);
        input.setStackInSlot(0, new ItemStack(Items.WHEAT_SEEDS, 2));
        water.setFluid(new FluidStack(Fluids.WATER, 400));
        energy.setEnergy(128000);
        fill(output, new ItemStack(Items.COBBLESTONE, 64));
        tick(helper, machine, 1);
        var expected = outputCounts(snapshot(machine));
        helper.assertTrue(expected.get(Items.WHEAT) == 2 && input.getStackInSlot(0).getCount() == 2,
                "A blocked greenhouse must prepare live mature loot without consuming seeds");
        machine = reload(helper, machine);
        clear(output);
        tick(helper, machine, 1100);
        helper.assertTrue(counts(output).equals(expected) && input.getStackInSlot(0).isEmpty()
                        && water.getInternalTank().getFluidInTank(0).isEmpty() && energy.getInternalEnergyStorage().getEnergyStored() == 0,
                "Reload must retain exact harvests and consume seeds, water and energy once");
        helper.succeed();
    }

    public static void greenhouseHarvestsUseLiveMatureLootAndKeepOverrides(GameTestHelper helper) {
        var base = new GreenhouseRecipe(id("greenhouse_live_test"), new ModRecipeData(
                new IngredientItem[] {new IngredientItem(new ItemStack(Items.WHEAT_SEEDS, 2), true)},
                new OutputItem[0], new FluidStack[0], new FluidStack[0], 128));
        var harvest = GreenhouseHarvestPreparation.harvest(base);
        helper.assertTrue(harvest.state().equals(((net.minecraft.world.level.block.CropBlock) Blocks.WHEAT).getStateForAge(7)) && harvest.count() == 2,
                "Wheat seeds must resolve to two fully mature harvests");
        var prepared = GreenhouseHarvestPreparation.prepare(base, helper.getLevel(), helper.absolutePos(BlockPos.ZERO));
        helper.assertTrue(outputCounts(prepared.getRecipe()).get(Items.WHEAT) == 2,
                "Live mature wheat loot must supply one wheat per planted seed");
        encode(helper, prepared.getRecipe());
        var staticData = base.getRecipe().copy();
        var override = new GreenhouseRecipe(base.getId(), new ModRecipeData(staticData.getIngredientItems(),
                new OutputItem[] {new OutputItem(new ItemStack(Items.DIAMOND, 7), 1.0f)},
                staticData.getFluidIngredients(), staticData.getFluidOutputs(), staticData.getTotalEnergy()));
        helper.assertTrue(GreenhouseHarvestPreparation.prepare(override, helper.getLevel(), BlockPos.ZERO) == override,
                "Explicit pack-authored outputs must bypass crop loot");
        var fertilized = new GreenhouseRecipe(base.getId(), new ModRecipeData(
                new IngredientItem[] {new IngredientItem(new ItemStack(Items.WHEAT_SEEDS, 2), true),
                        new IngredientItem(new ItemStack(ItemRegistries.FERTILIZER.get(), 2), true)},
                new OutputItem[0], new FluidStack[0], new FluidStack[0], 128));
        helper.assertTrue(GreenhouseHarvestPreparation.harvest(fertilized).count() == 3,
                "Fertilizer must provide 50 percent more harvests");
        var catalog = com.nstut.biotech.jei.SlaughterhouseLootSync.catalog(helper.getLevel());
        helper.assertTrue(catalog.getAsJsonArray("biotech:greenhouse_wheat").toString().contains("minecraft:wheat")
                        && catalog.getAsJsonArray("biotech:greenhouse_melon").toString().contains("minecraft:melon_slice"),
                "JEI must expose active mature-crop loot, including melon slices instead of a fabricated whole melon");
        helper.succeed();
    }

    public static void controllerDiagnosticScansAreBoundedAndEmptyRecipesAreDistinct(GameTestHelper helper) {
        var lootCatalog = com.nstut.biotech.jei.SlaughterhouseLootSync.catalog(helper.getLevel());
        helper.assertTrue(lootCatalog.getAsJsonArray("biotech:slaughterhouse_pig").toString().contains("minecraft:porkchop"),
                "Server JEI catalog must expose actual pig loot-table products");
        helper.assertTrue(lootCatalog.getAsJsonArray("biotech:slaughterhouse_cow").toString().contains("minecraft:leather"),
                "Server JEI catalog must expose actual cow loot-table products");
        Rig rig = slaughter(helper);
        var controller = new CountingDiagnosticController(rig.machine.getBlockPos(), rig.machine.getBlockState());
        helper.getLevel().setBlockEntity(controller);
        rig.machine = controller;
        tick(helper, controller, 100);
        helper.assertTrue(controller.scans == 1, "Repeated actual controller ticks in one world tick must scan once");
        helper.assertTrue(controller.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.MISSING_ITEMS,
                "Loaded recipes with empty inputs must report missing items");
        helper.runAfterDelay(19, () -> {
            tick(helper, controller, 100);
            helper.assertTrue(controller.scans == 1, "Idle world ticks 1 through 19 must reuse the initial scan");
        });
        helper.runAfterDelay(20, () -> {
            tick(helper, controller, 100);
            helper.assertTrue(controller.scans == 2, "World tick 20 must perform exactly one new diagnostic scan");
            controller.emptyRecipes = true;
        });
        helper.runAfterDelay(39, () -> {
            tick(helper, controller, 100);
            helper.assertTrue(controller.scans == 2, "Second interval must also remain bounded");
        });
        helper.runAfterDelay(40, () -> {
            tick(helper, controller, 100);
            helper.assertTrue(controller.scans == 3
                    && controller.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.NO_MATCHING_RECIPE,
                    "Empty recipe set and empty inventory must report no matching recipe");
            rig.animals.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        });
        helper.runAfterDelay(60, () -> {
            tick(helper, controller, 100);
            helper.assertTrue(controller.scans == 4
                    && controller.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.NO_MATCHING_RECIPE,
                    "Supplying items must not change the empty-recipe-set diagnosis");
            helper.succeed();
        });
    }

    /** Instrument only recipe enumeration; all structure, hatch and transaction ticks remain production code. */
    private static final class CountingDiagnosticController extends SlaughterhouseBlockEntity {
        private int scans;
        private boolean emptyRecipes;
        CountingDiagnosticController(BlockPos pos, BlockState state) { super(pos, state); }
        @Override protected <R extends com.nstut.nstutlib.recipes.ModRecipe<R>> List<R> diagnosticRecipes(
                net.minecraft.world.level.Level level, net.minecraft.world.item.crafting.RecipeType<R> type) {
            scans++;
            return emptyRecipes ? List.of() : super.diagnosticRecipes(level, type);
        }
    }

    public static void controllerDiagnosticsAndRedstonePreserveCycle(GameTestHelper helper) {
        Rig rig = slaughter(helper);
        var machine = (com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity) rig.machine;
        tick(helper, machine, 1);
        helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.MISSING_ITEMS, "Empty valid controller must report missing items");
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        var oldMenu = player.containerMenu;
        var menu = new com.nstut.biotech.views.machines.menu.SlaughterhouseMenu(91, player.getInventory(), machine);
        player.setPos(machine.getBlockPos().getX() + 0.5, machine.getBlockPos().getY() + 0.5, machine.getBlockPos().getZ() + 0.5);
        try {
            helper.assertTrue(!menu.clickMenuButton(player, 90), "A menu not open for this player must reject mode changes");
            player.containerMenu = menu;
            helper.assertTrue(!menu.clickMenuButton(player, -1), "Unknown button IDs must be rejected");
            helper.assertTrue(menu.clickMenuButton(player, 90) && machine.getRedstoneMode() == com.nstut.biotech.machines.RedstoneMode.HIGH, "Valid open menu must cycle the authoritative mode");
            player.setPos(machine.getBlockPos().getX() + 40, machine.getBlockPos().getY(), machine.getBlockPos().getZ());
            helper.assertTrue(!menu.clickMenuButton(player, 90), "Out-of-range player must not change the controller");
        } finally { player.containerMenu = oldMenu; machine.setRedstoneMode(com.nstut.biotech.machines.RedstoneMode.IGNORE); }

        rig.animals.setStackInSlot(0, animal(ItemRegistries.COW.get(), "cow", -1, "controlled_slaughter"));
        tick(helper, machine, 1);
        helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.MISSING_ITEMS,
                "Input changes must retain the cached diagnosis within the refresh interval");
        helper.runAfterDelay(19, () -> {
            var idle = (com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity) rig.machine;
            tick(helper, idle, 1);
            helper.assertTrue(idle.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.MISSING_ITEMS,
                    "Diagnosis must remain cached through world tick 19");
        });
        helper.runAfterDelay(20, () -> {
            var idle = (com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity) rig.machine;
            tick(helper, idle, 1);
            helper.assertTrue(idle.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.MISSING_FLUID,
                    "Matching cow without water must refresh to missing fluid after 20 world ticks");
            finishControllerRedstoneCycle(helper, rig);
        });
    }

    private static void finishControllerRedstoneCycle(GameTestHelper helper, Rig rig) {
        var machine = (com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity) rig.machine;
        rig.water.setFluid(new FluidStack(Fluids.WATER, 200));
        fill(rig.outputs, new ItemStack(Items.COBBLESTONE, 64));
        tick(helper, machine, 1);
        helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.ITEM_OUTPUT_BLOCKED, "Exact prepared loot must report blocked item output");
        int[] rolls = ((int[]) field(machine, "activeItemOutputIndexes")).clone();
        ModRecipeData original = snapshot(machine).copy();
        machine.setRedstoneMode(com.nstut.biotech.machines.RedstoneMode.HIGH);
        clear(rig.outputs);
        rig.energy.setEnergy(10000);
        tick(helper, machine, 3);
        helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.REDSTONE_PAUSED && !rig.animals.getStackInSlot(0).isEmpty(), "Paused unconsumed transaction must retain inputs");
        rig.machine = reload(helper, machine);
        machine = (com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity) rig.machine;
        tick(helper, machine, 1);
        helper.assertTrue(machine.getRedstoneMode() == com.nstut.biotech.machines.RedstoneMode.HIGH && Arrays.equals(rolls, (int[]) field(machine, "activeItemOutputIndexes")), "Mode and exact output rolls must survive save/load while paused");
        helper.assertTrue(machine.getDisplayRecipe() != null && machine.getDisplayRecipe().getTotalEnergy() == original.getTotalEnergy()
                && outputCounts(machine.getDisplayRecipe()).equals(outputCounts(original)), "Paused reload must expose the exact saved recipe to the menu before resuming");
        machine.setRedstoneMode(com.nstut.biotech.machines.RedstoneMode.LOW);
        rig.energy.setEnergy(0);
        tick(helper, machine, 1);
        helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.INSUFFICIENT_ENERGY && rig.animals.getStackInSlot(0).isEmpty(), "Enabled cycle consumes inputs once then reports lack of energy");
        machine.setRedstoneMode(com.nstut.biotech.machines.RedstoneMode.HIGH);
        rig.energy.setEnergy(10000);
        tick(helper, machine, 3);
        helper.assertTrue((int) field(machine, "energyConsumed") == 0 && rig.energy.getInternalEnergyStorage().getEnergyStored() == 10000, "Paused committed cycle must not draw energy");
        BlockPos signal = machine.getBlockPos().relative(Direction.SOUTH);
        helper.getLevel().setBlock(signal, Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        tick(helper, machine, 1);
        helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.PROCESSING && (int) field(machine, "energyConsumed") > 0, "High mode must resume with a real neighbor signal");
        machine.setRedstoneMode(com.nstut.biotech.machines.RedstoneMode.LOW);
        int progress = (int) field(machine, "energyConsumed");
        tick(helper, machine, 2);
        helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.REDSTONE_PAUSED && (int) field(machine, "energyConsumed") == progress, "Low mode must pause while powered");
        machine.setRedstoneMode(com.nstut.biotech.machines.RedstoneMode.IGNORE);
        tick(helper, machine, 1);
        helper.assertTrue((int) field(machine, "energyConsumed") > progress && outputCounts(snapshot(machine)).equals(outputCounts(original)), "Ignore mode resumes the same exact products even while powered");
        helper.succeed();
    }

    public static void controllerBalanceSnapshotAndFluidDiagnostics(GameTestHelper helper) {
        Rig rig = habitat(helper);
        rig.animals.setStackInSlot(0, animal(ItemRegistries.COW.get(), "cow", -1, null));
        rig.food.setStackInSlot(0, new ItemStack(Items.WHEAT, 4));
        rig.water.setFluid(new FluidStack(Fluids.WATER, 1000));
        rig.milk.setFluid(new FluidStack(Fluids.LAVA, 1000));
        double oldMultiplier = Config.machineEnergyMultiplier;
        int oldRate = Config.machineEnergyPerTick;
        try {
            Config.machineEnergyMultiplier = 2.0;
            Config.machineEnergyPerTick = 37;
            rig.energy.setEnergy(10000);
            tick(helper, rig.machine, 1);
            var machine = (com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity) rig.machine;
            helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.FLUID_OUTPUT_BLOCKED, "Incompatible milk output tank must report fluid output blocked");
            int cost = snapshot(machine).getTotalEnergy();
            helper.assertTrue(cost == 64000 && (int) field(machine, "energyConsumed") == 0, "Blocked output must not consume any energy");
            Config.machineEnergyMultiplier = 0.5;
            rig.machine = reload(helper, machine);
            rig.milk.setFluid(FluidStack.EMPTY);
            tick(helper, rig.machine, 1);
            helper.assertTrue(snapshot(rig.machine).getTotalEnergy() == cost && (int) field(rig.machine, "energyConsumed") == 37, "Reload/config change keeps active total cost and obeys configured throughput");
        } finally { Config.machineEnergyMultiplier = oldMultiplier; Config.machineEnergyPerTick = oldRate; }
        helper.succeed();
    }

    public static void lootPolicySeedAndStackLimits(GameTestHelper helper) {
        SlaughterhouseRecipe recipe = slaughterRecipe(helper, "cow");
        ItemStackHandler inputs = new ItemStackHandler(2);
        inputs.setStackInSlot(0, animal(ItemRegistries.COW.get(), "cow", -1, "controlled_slaughter"));
        int oldMultiplier = Config.slaughterhouseYieldMultiplier;
        try {
            Config.slaughterhouseYieldMultiplier = 2;
            SlaughterhouseRecipe first = SlaughterhouseLootPreparation.prepare(recipe, inputs, helper.getLevel(), helper.absolutePos(BlockPos.ZERO));
            SlaughterhouseRecipe again = SlaughterhouseLootPreparation.prepare(recipe, inputs, helper.getLevel(), helper.absolutePos(BlockPos.ZERO));
            Map<Item, Integer> counts = outputCounts(first.getRecipe());
            helper.assertTrue(counts.getOrDefault(Items.DIAMOND, 0) == 128, "Concrete cow DeathLootTable must override its type default and split 64 x 2 into legal stacks");
            helper.assertTrue(!counts.containsKey(Items.NETHER_STAR), "A non-player slaughter context must not grant player-only loot");
            helper.assertTrue(counts.equals(outputCounts(again.getRecipe())), "A captured DeathLootTableSeed must deterministically control the probabilistic fixture");
            helper.assertTrue(first.getItemOutputs().stream().allMatch(o -> o.getChance() == 1.0f && o.getItemStack().getCount() <= o.getItemStack().getMaxStackSize()), "Prepared loot must be already rolled and legally stack-sized");
            encode(helper, first.getRecipe());
            ItemStack extra = inputs.getStackInSlot(0).copy();
            inputs.setStackInSlot(1, extra);
            SlaughterhouseRecipe pair = recipe.create(recipe.getId(), new ModRecipeData(
                    new IngredientItem[]{new IngredientItem(new ItemStack(ItemRegistries.COW.get(), 2), true)},
                    new OutputItem[0], new FluidStack[0], new FluidStack[0], 0));
            helper.assertTrue(outputCounts(SlaughterhouseLootPreparation.prepare(pair, inputs, helper.getLevel(), helper.absolutePos(BlockPos.ZERO)).getRecipe()).get(Items.DIAMOND) == 256,
                    "A dynamic recipe must roll loot for every consumed animal, including allocations spanning slots");
            inputs.setStackInSlot(1, animal(ItemRegistries.SHEEP.get(), "sheep", 14, null));
            SlaughterhouseRecipe catalyst = recipe.create(recipe.getId(), new ModRecipeData(
                    new IngredientItem[]{new IngredientItem(new ItemStack(ItemRegistries.COW.get()), true), new IngredientItem(new ItemStack(ItemRegistries.SHEEP.get()), false)},
                    new OutputItem[0], new FluidStack[0], new FluidStack[0], 0));
            helper.assertTrue(!outputCounts(SlaughterhouseLootPreparation.prepare(catalyst, inputs, helper.getLevel(), helper.absolutePos(BlockPos.ZERO)).getRecipe()).containsKey(Items.RED_WOOL),
                    "Retained animal catalysts must never grant death loot");
            inputs.setStackInSlot(1, ItemStack.EMPTY);
            ItemStack redSheep = animal(ItemRegistries.SHEEP.get(), "sheep", 14, null);
            CompoundTag sheared = CapturedAnimalStackState.read(redSheep);
            sheared.putBoolean("Sheared", true);
            CapturedAnimalStackState.writeCapture(redSheep, sheared, "minecraft:sheep", 14);
            inputs.setStackInSlot(0, redSheep);
            helper.assertTrue(!outputCounts(SlaughterhouseLootPreparation.prepare(slaughterRecipe(helper, "sheep"), inputs, helper.getLevel(), helper.absolutePos(BlockPos.ZERO)).getRecipe()).containsKey(Items.RED_WOOL),
                    "A captured sheared sheep must use its state-dependent table and must not regenerate death wool");
            inputs.setStackInSlot(0, extra);
            Config.slaughterhouseYieldMultiplier = 3;
            helper.assertTrue(outputCounts(SlaughterhouseLootPreparation.prepare(recipe, inputs, helper.getLevel(), helper.absolutePos(BlockPos.ZERO)).getRecipe()).get(Items.DIAMOND) == 192, "Server yield configuration must affect new dynamic transactions");
            SlaughterhouseRecipe explicit = recipe.create(recipe.getId(), new ModRecipeData(recipe.getRecipe().getIngredientItems(), new OutputItem[]{new OutputItem(new ItemStack(Items.COAL, 7), 0.25f)}, new FluidStack[0], new FluidStack[0], 0));
            helper.assertTrue(!explicit.usesEntityLoot() && SlaughterhouseLootPreparation.prepare(explicit, inputs, helper.getLevel(), helper.absolutePos(BlockPos.ZERO)) == explicit, "Explicit static products and their chances must remain exactly what JEI shows");
            OutputItem[] swords = AmplifiedLootOutputs.split(List.of(new ItemStack(Items.DIAMOND_SWORD)), 2);
            helper.assertTrue(swords.length == 2 && swords[0].getItemStack().getCount() == 1, "Unstackable loot must also split legally");
        } finally {
            Config.slaughterhouseYieldMultiplier = oldMultiplier;
        }
        helper.succeed();
    }

    public static void slaughterBlockedPauseAndReloadPreserveExactLoot(GameTestHelper helper) {
        Rig rig = slaughter(helper);
        rig.animals.setStackInSlot(0, animal(ItemRegistries.COW.get(), "cow", -1, "controlled_slaughter"));
        rig.water.setFluid(new FluidStack(Fluids.WATER, 200));
        rig.energy.setEnergy(128);
        fill(rig.outputs, new ItemStack(Items.COBBLESTONE, 64));
        tick(helper, rig.machine, 1);
        helper.assertTrue(rig.animals.getStackInSlot(0).getCount() == 1 && rig.water.getInternalTank().getFluidInTank(0).getAmount() == 200, "Full outputs must not consume the cow or water");
        ModRecipeData snapshot = snapshot(rig.machine);
        Map<Item, Integer> expected = outputCounts(snapshot);
        int[] rolls = ((int[]) field(rig.machine, "activeItemOutputIndexes")).clone();
        helper.assertTrue(expected.getOrDefault(Items.DIAMOND, 0) == 64 * SlaughterhouseLootPreparation.getYieldMultiplier(), "Prepared custom table must be recorded before waiting for space");
        rig.machine = reload(helper, rig.machine);
        tick(helper, rig.machine, 2);
        helper.assertTrue(outputCounts(snapshot(rig.machine)).equals(expected), "Blocked reload must keep the same prepared output");
        clear(rig.outputs);
        tick(helper, rig.machine, 1);
        helper.assertTrue(rig.animals.getStackInSlot(0).isEmpty() && rig.water.getInternalTank().getFluidInTank(0).isEmpty(), "Transaction must consume the animal and water exactly once");
        helper.assertTrue((int) field(rig.machine, "energyConsumed") == 128, "Transaction must pause after consuming only available power");
        BlockPos broken = rig.machine.getBlockPos().above();
        BlockState old = helper.getLevel().getBlockState(broken);
        helper.assertTrue(!old.isAir(), "Test must break an actual structural block");
        helper.getLevel().setBlock(broken, Blocks.AIR.defaultBlockState(), 3);
        rig.machine.requestStructureValidation();
        tick(helper, rig.machine, 1);
        helper.assertTrue((int) field(rig.machine, "energyConsumed") == 128, "Broken structure must preserve transaction progress");
        helper.assertTrue(!(boolean) field(rig.machine, "isStructureValid"), "Broken structure must actually invalidate the machine");
        helper.getLevel().setBlock(broken, old, 3);
        rig.machine.requestStructureValidation();
        tick(helper, rig.machine, 2);
        helper.assertTrue(Arrays.equals(rolls, (int[]) field(rig.machine, "activeItemOutputIndexes")), "Pause and invalid structure must not reroll any output");
        rig.machine = reload(helper, rig.machine);
        helper.assertTrue(outputCounts(snapshot(rig.machine)).equals(expected), "The legal 128-item amplified result must survive snapshot persistence");
        rig.energy.setEnergy(16000 - 128);
        tick(helper, rig.machine, 80);
        helper.assertTrue(counts(rig.outputs).equals(expected), "Reload must emit exactly the originally rolled loot, without loss or duplication");
        helper.assertTrue(rig.energy.getInternalEnergyStorage().getEnergyStored() == 0 && rig.water.getInternalTank().getFluidInTank(0).isEmpty(), "One slaughter must cost exactly 16000 FE and 200 mB, even after reload");
        tick(helper, rig.machine, 3);
        helper.assertTrue(counts(rig.outputs).equals(expected), "Completed output must not be replayed by later ticks");
        helper.succeed();
    }

    public static void blockedDonorReplacementAndOversizeAreSafe(GameTestHelper helper) {
        Rig rig = slaughter(helper);
        rig.animals.setStackInSlot(0, animal(ItemRegistries.SHEEP.get(), "sheep", 14, null));
        rig.water.setFluid(new FluidStack(Fluids.WATER, 200));
        rig.energy.setEnergy(16000);
        fill(rig.outputs, new ItemStack(Items.COBBLESTONE, 64));
        tick(helper, rig.machine, 1);
        helper.assertTrue(outputCounts(snapshot(rig.machine)).containsKey(Items.RED_WOOL), "Initial donor must prepare red wool");
        rig.animals.setStackInSlot(0, animal(ItemRegistries.SHEEP.get(), "sheep", 0, null));
        tick(helper, rig.machine, 1);
        helper.assertTrue(rig.animals.getStackInSlot(0).getCount() == 1 && rig.water.getInternalTank().getFluidInTank(0).getAmount() == 200, "Replacing the donor without an empty tick must cancel the old transaction without consuming the replacement");
        clear(rig.outputs);
        tick(helper, rig.machine, 80);
        helper.assertTrue(counts(rig.outputs).getOrDefault(Items.RED_WOOL, 0) == 0 && counts(rig.outputs).getOrDefault(Items.WHITE_WOOL, 0) == SlaughterhouseLootPreparation.getYieldMultiplier(), "Replacement must create a fresh white-sheep transaction");
        clear(rig.outputs);
        rig.animals.setStackInSlot(0, animal(ItemRegistries.COW.get(), "cow", -1, "excessive_slaughter"));
        rig.water.setFluid(new FluidStack(Fluids.WATER, 200));
        rig.energy.setEnergy(16000);
        tick(helper, rig.machine, 1);
        helper.assertTrue(rig.animals.getStackInSlot(0).getCount() == 1 && rig.water.getInternalTank().getFluidInTank(0).getAmount() == 200 && rig.energy.getInternalEnergyStorage().getEnergyStored() == 16000, "More than 256 output stacks must be safely rejected by the real machine before any resources are consumed");
        helper.assertTrue(counts(rig.outputs).isEmpty(), "Rejected oversize loot must not leak partial output");
        helper.assertTrue((boolean) field(rig.machine, "isStructureValid"), "Preparation rejection must preserve the valid structure");
        helper.assertTrue((int) field(rig.machine, "processingFailureCooldown") == 20, "Rejected preparation must enter the provider retry cooldown");
        for (int retry = 0; retry < 3; retry++) {
            for (int remaining = 19; remaining >= 0; remaining--) {
                tick(helper, rig.machine, 1);
                helper.assertTrue((int) field(rig.machine, "processingFailureCooldown") == remaining,
                        "Unchanged oversized input must not regenerate loot or reset cooldown each tick");
            }
            tick(helper, rig.machine, 1);
            helper.assertTrue((int) field(rig.machine, "processingFailureCooldown") == 20,
                    "Only an expired cooldown may retry unchanged rejected input");
            helper.assertTrue((boolean) field(rig.machine, "isStructureValid") && snapshot(rig.machine) == null,
                    "Repeated rejection must preserve structure without starting a transaction");
            helper.assertTrue(rig.animals.getStackInSlot(0).getCount() == 1
                    && rig.water.getInternalTank().getFluidInTank(0).getAmount() == 200
                    && rig.energy.getInternalEnergyStorage().getEnergyStored() == 16000
                    && counts(rig.outputs).isEmpty(), "Sustained rejection must preserve all resources and outputs");
        }
        rig.animals.setStackInSlot(0, animal(ItemRegistries.COW.get(), "cow", -1, "controlled_slaughter"));
        tick(helper, rig.machine, 100);
        helper.assertTrue(rig.animals.getStackInSlot(0).isEmpty() && !counts(rig.outputs).isEmpty(),
                "Replacing rejected input must recover after the bounded cooldown");
        helper.succeed();
    }

    public static void renewableItemsRepeatWithExactCosts(GameTestHelper helper) {
        Rig rig = habitat(helper);
        for (boolean sheep : new boolean[]{false, true}) {
            ItemStack donor = animal(sheep ? ItemRegistries.SHEEP.get() : ItemRegistries.CHICKEN.get(), sheep ? "sheep" : "chicken", sheep ? 14 : -1, null);
            rig.animals.setStackInSlot(0, donor.copy());
            rig.food.setStackInSlot(0, new ItemStack(sheep ? Items.WHEAT : Items.WHEAT_SEEDS, 4));
            rig.water.setFluid(new FluidStack(Fluids.WATER, 500));
            rig.energy.setEnergy(64000);
            fill(rig.outputs, new ItemStack(Items.COBBLESTONE, 64));
            tick(helper, rig.machine, 1);
            helper.assertTrue(rig.food.getStackInSlot(0).getCount() == 4 && rig.water.getInternalTank().getFluidInTank(0).getAmount() == 500 && rig.energy.getInternalEnergyStorage().getEnergyStored() == 64000, "Blocked renewable item output must preserve food, water and power");
            clear(rig.outputs);
            tick(helper, rig.machine, 160);
            helper.assertTrue(same(donor, rig.animals.getStackInSlot(0)), "Repeated production must retain the exact adult state and count");
            helper.assertTrue(rig.food.getStackInSlot(0).isEmpty() && rig.water.getInternalTank().getFluidInTank(0).isEmpty() && rig.energy.getInternalEnergyStorage().getEnergyStored() == 0, "Two renewable cycles must consume exactly four food, 500 mB and 64000 FE");
            helper.assertTrue(counts(rig.outputs).getOrDefault(sheep ? Items.RED_WOOL : Items.EGG, 0) == 2 && counts(rig.outputs).getOrDefault(ItemRegistries.MANURE.get(), 0) == 2, "Two cycles must deliver exactly two products and two manure");
            clear(rig.outputs);
        }
        helper.succeed();
    }

    public static void renewableMilkBlocksAndReloadsWithoutConsumingAdult(GameTestHelper helper) {
        helper.assertTrue(NeoForgeMod.MILK.get() != Fluids.EMPTY, "Biotech must explicitly enable loader milk on a minimal installation");
        Rig rig = habitat(helper);
        ItemStack donor = animal(ItemRegistries.COW.get(), "cow", -1, null);
        rig.animals.setStackInSlot(0, donor.copy());
        rig.food.setStackInSlot(0, new ItemStack(Items.WHEAT, 4));
        rig.water.setFluid(new FluidStack(Fluids.WATER, 500));
        rig.energy.setEnergy(128);
        for (FluidStack blocked : List.of(new FluidStack(Fluids.LAVA, 1), new FluidStack(NeoForgeMod.MILK.get(), rig.milk.getInternalTank().getTankCapacity(0)))) {
            rig.milk.setFluid(blocked);
            tick(helper, rig.machine, 2);
            helper.assertTrue(rig.food.getStackInSlot(0).getCount() == 4 && rig.water.getInternalTank().getFluidInTank(0).getAmount() == 500 && rig.energy.getInternalEnergyStorage().getEnergyStored() == 128, "Wrong-fluid and full milk tanks must block all consumption");
        }
        rig.milk.setFluid(FluidStack.EMPTY);
        tick(helper, rig.machine, 1);
        helper.assertTrue(rig.food.getStackInSlot(0).getCount() == 2 && rig.water.getInternalTank().getFluidInTank(0).getAmount() == 250, "First cycle must reserve its resources only once");
        rig.machine = reload(helper, rig.machine);
        rig.energy.setEnergy(64000 - 128);
        tick(helper, rig.machine, 160);
        helper.assertTrue(same(donor, rig.animals.getStackInSlot(0)), "Milk production and reload must retain the exact captured cow");
        helper.assertTrue(rig.milk.getInternalTank().getFluidInTank(0).getFluid() == NeoForgeMod.MILK.get() && rig.milk.getInternalTank().getFluidInTank(0).getAmount() == 2000, "Two loaded default recipes must deliver 2000 mB of registered milk through the actual fluid output hatch");
        helper.assertTrue(rig.food.getStackInSlot(0).isEmpty() && rig.water.getInternalTank().getFluidInTank(0).isEmpty() && rig.energy.getInternalEnergyStorage().getEnergyStored() == 0 && counts(rig.outputs).getOrDefault(ItemRegistries.MANURE.get(), 0) == 2, "Reloaded milk cycles must charge exact food, water and FE and emit manure once per cycle");
        helper.succeed();
    }

    public static void goatDatapackExampleDecodesAndRepeats(GameTestHelper helper) {
        ModRecipeData data;
        try (var stream = LivestockProductionGameTests.class.getResourceAsStream("/biotech_examples/goat_milk.json")) {
            if (stream == null) throw new AssertionError("Missing goat datapack example");
            data = ModRecipeData.CODEC.parse(helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE), JsonParser.parseReader(new InputStreamReader(stream))).result().orElseThrow(() -> new AssertionError("Target-specific goat example must decode with the production recipe codec"));
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        }
        TerrestrialHabitatRecipe recipe = new TerrestrialHabitatRecipe(id("gametest_goat"), data);
        ItemStack donor = animal(ItemRegistries.CAPTURED_ANIMAL.get(), "goat", -1, null);
        ItemStackHandler inputs = new ItemStackHandler(2);
        inputs.setStackInSlot(0, donor.copy());
        inputs.setStackInSlot(1, new ItemStack(Items.WHEAT, 4));
        ItemStackHandler outputs = new ItemStackHandler(4);
        FluidTank water = new FluidTank(1000);
        water.setFluid(new FluidStack(Fluids.WATER, 500));
        FluidTank milk = new FluidTank(4000);
        for (int cycle = 0; cycle < 2; cycle++) {
            var prepared = AnimalRecipeStatePreparation.prepareHabitat(recipe, inputs);
            helper.assertTrue(prepared.recipeMatch(inputs, List.of(water), outputs, List.of(milk)), "The data-driven generic adult goat selector must match without hardcoded species production logic");
            helper.assertTrue(prepared.tryConsumeIngredients(inputs, List.of(water)), "Goat example must consume its authored resources");
            prepared.assemble(outputs, List.of(milk), prepared.rollItemOutputIndexes());
        }
        helper.assertTrue(same(donor, inputs.getStackInSlot(0)) && inputs.getStackInSlot(1).isEmpty() && water.isEmpty() && milk.getFluidAmount() == 2000, "Generic-species extension must repeat while retaining the donor");
        helper.succeed();
    }

    public static void datapackReloadKeepsInFlightSlaughter(GameTestHelper helper) {
        Rig rig = slaughter(helper);
        rig.animals.setStackInSlot(0, animal(ItemRegistries.COW.get(), "cow", -1, "controlled_slaughter"));
        rig.water.setFluid(new FluidStack(Fluids.WATER, 200));
        rig.energy.setEnergy(128);
        tick(helper, rig.machine, 1);
        Map<Item, Integer> expected = outputCounts(snapshot(rig.machine));
        CompletableFuture<Void> reloading = helper.getLevel().getServer().reloadResources(helper.getLevel().getServer().getPackRepository().getSelectedIds());
        helper.runAfterDelay(1, new Runnable() {
            @Override public void run() {
                if (!reloading.isDone()) { helper.runAfterDelay(1, this); return; }
                reloading.join();
                rig.machine = reload(helper, rig.machine);
                rig.energy.setEnergy(16000 - 128);
                tick(helper, rig.machine, 80);
                helper.assertTrue(counts(rig.outputs).equals(expected) && rig.animals.getStackInSlot(0).isEmpty() && rig.water.getInternalTank().getFluidInTank(0).isEmpty(), "A real datapack reload followed by block-entity reload must complete the original prepared loot without consuming another animal");
                helper.succeed();
            }
        });
    }

    private static ItemStack animal(Item item, String type, int color, String loot) {
        ItemStack stack = new ItemStack(item);
        CompoundTag state = new CompoundTag();
        state.putInt("Age", 0);
        if (color >= 0) state.putByte("Color", (byte) color);
        if (loot != null) {
            state.putString("DeathLootTable", "biotech:gametest/" + loot);
            state.putLong("DeathLootTableSeed", 739391L);
        }
        CapturedAnimalStackState.writeCapture(stack, state, "minecraft:" + type, color);
        return stack;
    }

    private static SlaughterhouseRecipe slaughterRecipe(GameTestHelper helper, String animal) {
        return (SlaughterhouseRecipe) helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, id("slaughterhouse_" + animal))).orElseThrow().value();
    }

    private static net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipeKey(String path) { return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id(path)); }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(Biotech.MOD_ID, path); }
    private static boolean same(ItemStack a, ItemStack b) { return ItemStack.matches(a, b); }
    private static void encode(GameTestHelper helper, ModRecipeData data) {
        helper.assertTrue(ModRecipeData.CODEC.encodeStart(helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE), data).result().isPresent(), "Prepared output snapshot must encode successfully, including amplified counts");
    }
    private static Map<Item, Integer> outputCounts(ModRecipeData data) {
        Map<Item, Integer> counts = new HashMap<>();
        for (OutputItem o : data.getOutputItems()) counts.merge(o.getItemStack().getItem(), o.getItemStack().getCount(), Integer::sum);
        return counts;
    }
    private static Map<Item, Integer> counts(IItemHandler items) {
        Map<Item, Integer> counts = new HashMap<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        return counts;
    }
    private static void fill(IItemHandlerModifiable items, ItemStack stack) { for (int i = 0; i < items.getSlots(); i++) items.setStackInSlot(i, stack.copy()); }
    private static void clear(IItemHandlerModifiable items) { fill(items, ItemStack.EMPTY); }
    private static ModRecipeData snapshot(MachineBlockEntity machine) { return (ModRecipeData) field(machine, "activeRecipeSnapshot"); }
    private static Object field(MachineBlockEntity machine, String name) {
        try { Field f = MachineBlockEntity.class.getDeclaredField(name); f.setAccessible(true); return f.get(machine); }
        catch (ReflectiveOperationException exception) { throw new AssertionError(exception); }
    }
    private static void tick(GameTestHelper helper, MachineBlockEntity machine, int count) {
        for (int i = 0; i < count; i++) MachineBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), helper.getLevel().getBlockState(machine.getBlockPos()), machine);
    }
    private static MachineBlockEntity reload(GameTestHelper helper, MachineBlockEntity old) {
        CompoundTag saved = old.saveWithFullMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(saved.contains("activeRecipeSnapshot"), "In-flight recipe snapshot must be present on disk");
        BlockState state = helper.getLevel().getBlockState(old.getBlockPos());
        MachineBlockEntity reloaded = old instanceof SlaughterhouseBlockEntity ? new SlaughterhouseBlockEntity(old.getBlockPos(), state) : old instanceof GreenhouseBlockEntity ? new GreenhouseBlockEntity(old.getBlockPos(), state) : new TerrestrialHabitatBlockEntity(old.getBlockPos(), state);
        reloaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
        helper.getLevel().setBlockEntity(reloaded);
        return reloaded;
    }
    private static Rig slaughter(GameTestHelper helper) {
        MachineBlockEntity machine = place(helper, false);
        BlockPos p = machine.getBlockPos();
        return new Rig(machine, block(helper, p.offset(-2, -1, -1), ItemInputHatchBlockEntity.class).getInternalItemStorage(), block(helper, p.offset(-2, -1, -3), ItemInputHatchBlockEntity.class).getInternalItemStorage(), block(helper, p.offset(2, -1, -2), ItemOutputHatchBlockEntity.class).getInternalItemStorage(), block(helper, p.offset(-1, -1, -4), FluidInputHatchBlockEntity.class), null, block(helper, p.offset(1, -1, -4), EnergyInputHatchBlockEntity.class));
    }
    private static Rig habitat(GameTestHelper helper) {
        MachineBlockEntity machine = place(helper, true);
        BlockPos p = machine.getBlockPos();
        return new Rig(machine, block(helper, p.offset(-3, -1, -1), ItemInputHatchBlockEntity.class).getInternalItemStorage(), block(helper, p.offset(-3, -1, -3), ItemInputHatchBlockEntity.class).getInternalItemStorage(), block(helper, p.offset(3, -1, -3), ItemOutputHatchBlockEntity.class).getInternalItemStorage(), block(helper, p.offset(-2, -1, -6), FluidInputHatchBlockEntity.class), block(helper, p.offset(2, -1, -6), FluidOutputHatchBlockEntity.class), block(helper, p.offset(0, -1, -6), EnergyInputHatchBlockEntity.class));
    }
    private static MachineBlockEntity place(GameTestHelper helper, boolean habitat) { return place(helper, habitat ? 1 : 0); }
    private static MachineBlockEntity place(GameTestHelper helper, int kind) {
        BlockPos pos = helper.absolutePos(new BlockPos(8, 2, 8));
        BlockState controller = (kind == 2 ? MachineRegistries.GREENHOUSE : kind == 1 ? MachineRegistries.TERRESTRIAL_HABITAT : MachineRegistries.SLAUGHTERHOUSE).block().get().defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
        MachineBlockEntity blueprint = kind == 2 ? new GreenhouseBlockEntity(pos, controller) : kind == 1 ? new TerrestrialHabitatBlockEntity(pos, controller) : new SlaughterhouseBlockEntity(pos, controller);
        MultiblockBlock[][][] pattern = blueprint.getMultiblockPattern().getPattern();
        for (int y = 0; y < pattern.length; y++) for (int z = 0; z < pattern[y].length; z++) for (int x = 0; x < pattern[y][z].length; x++) {
            MultiblockBlock expected = pattern[y][z][x];
            if (expected == null) continue;
            BlockPos target = MultiblockPattern.rotateBlockPos(pos, blueprint.getSouthOffsetX(), blueprint.getSouthOffsetY(), blueprint.getSouthOffsetZ(), pattern.length, pattern[y].length, x, y, z, controller);
            BlockState state = expected.getBlock().defaultBlockState();
            for (var entry : expected.getStates().entrySet()) state = property(state, state.getBlock().getStateDefinition().getProperty(entry.getKey()), entry.getValue());
            helper.getLevel().setBlock(target, state, 3);
        }
        MachineBlockEntity machine = block(helper, pos, MachineBlockEntity.class);
        helper.assertTrue(machine.checkMultiblock(helper.getLevel(), pos, helper.getLevel().getBlockState(pos)), "Real livestock machine structure must be valid before testing transactions");
        return machine;
    }
    private static <T extends Comparable<T>> BlockState property(BlockState state, Property<T> property, String value) { return state.setValue(property, property.getValue(value).orElseThrow()); }
    private static <T extends BlockEntity> T block(GameTestHelper helper, BlockPos pos, Class<T> type) {
        BlockEntity entity = helper.getLevel().getBlockEntity(pos);
        if (!type.isInstance(entity)) throw new AssertionError("Missing " + type.getSimpleName() + " at " + pos);
        return type.cast(entity);
    }
    private static final class Rig {
        MachineBlockEntity machine;
        final IItemHandlerModifiable animals, food, outputs;
        final FluidHatchBlockEntity water, milk;
        final EnergyInputHatchBlockEntity energy;
        Rig(MachineBlockEntity machine, IItemHandlerModifiable animals, IItemHandlerModifiable food, IItemHandlerModifiable outputs, FluidHatchBlockEntity water, FluidHatchBlockEntity milk, EnergyInputHatchBlockEntity energy) {
            this.machine = machine; this.animals = animals; this.food = food; this.outputs = outputs; this.water = water; this.milk = milk; this.energy = energy;
        }
    }
}
