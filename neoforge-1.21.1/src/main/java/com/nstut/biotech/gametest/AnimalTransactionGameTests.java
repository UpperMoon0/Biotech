package com.nstut.biotech.gametest;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.entites.hatches.*;
import com.nstut.biotech.blocks.entites.machines.BreedingChamberBlockEntity;
import com.nstut.biotech.blocks.entites.machines.TerrestrialHabitatBlockEntity;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.items.MobItem;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.recipes.AnimalRecipeStatePreparation;
import com.nstut.biotech.recipes.TerrestrialHabitatRecipe;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Biotech.MOD_ID)
@PrefixGameTestTemplate(false)
/** Runs the real livestock controller, hatches, recipe selection, consumption and snapshot reload. */
public final class AnimalTransactionGameTests {
    private AnimalTransactionGameTests() {}

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void habitatWoolPreservesAuthoredMetadataAcrossSnapshot(GameTestHelper helper) {
        for (int color : new int[]{0, 14}) {
            ItemStack authored = new ItemStack(Items.WHITE_WOOL, 3);
            authored.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Fine wool"));
            authored.set(DataComponents.MAX_STACK_SIZE, 16);
            authored.remove(DataComponents.RARITY);
            CustomData.update(DataComponents.CUSTOM_DATA, authored, root -> {
                root.putString("Grade", "fine");
                CompoundTag provenance = new CompoundTag();
                provenance.putString("source", "datapack");
                root.put("Provenance", provenance);
            });
            ItemStack original = authored.copy();
            ItemStack donor = sheep(false, color, "Wool donor");
            ItemStackHandler inputs = new ItemStackHandler(1);
            inputs.setStackInSlot(0, donor.copy());
            TerrestrialHabitatRecipe recipe = new TerrestrialHabitatRecipe(
                    ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "test_tagged_wool_" + color),
                    new ModRecipeData(new IngredientItem[]{new IngredientItem(new ItemStack(ItemRegistries.SHEEP.get()), false)},
                            new OutputItem[]{new OutputItem(authored, 1.0f)},
                            new FluidStack[0], new FluidStack[0], 0));

            var variants = AnimalRecipeStatePreparation.habitatOutputVariants(recipe, authored);
            helper.assertTrue(variants.size() == 16, "Sheep wool presentation must expose every dye color");
            helper.assertTrue(variants.stream().allMatch(v -> v.getCount() == original.getCount()),
                    "Wool alternatives must retain the authored quantity");
            helper.assertTrue(variants.stream().anyMatch(v -> v.is(Items.RED_WOOL)), "Red wool must be discoverable");
            helper.assertTrue(AnimalRecipeStatePreparation.habitatOutputVariants(recipe, new ItemStack(Items.EGG)).size() == 1,
                    "Non-wool outputs must not gain wool alternatives");
            ModRecipeData prepared = AnimalRecipeStatePreparation.prepareHabitat(recipe, inputs).getRecipe();
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var encoded = ModRecipeData.CODEC.encodeStart(ops, prepared).result().orElseThrow();
            ModRecipeData restored = ModRecipeData.CODEC.parse(ops, encoded).result().orElseThrow();
            TerrestrialHabitatRecipe reloaded = recipe.create(recipe.getId(), restored);
            ItemStackHandler destination = new ItemStackHandler(1);
            helper.assertTrue(reloaded.tryConsumeIngredients(inputs, List.of()), "Saved wool recipe must accept its retained donor");
            reloaded.assemble(destination, List.of(), reloaded.rollItemOutputIndexes());

            ItemStack actual = destination.getStackInSlot(0);
            Item expectedItem = color == 0 ? Items.WHITE_WOOL : Items.RED_WOOL;
            helper.assertTrue(variants.stream().anyMatch(v -> same(v, actual)),
                    "Indexed wool alternatives must match the complete runtime product metadata");
            helper.assertTrue(actual.is(expectedItem) && actual.getCount() == 3,
                    "Wool recoloring and snapshot reload must preserve the authored count for white and red sheep");
            helper.assertTrue("Fine wool".equals(actual.getHoverName().getString())
                            && original.getComponentsPatch().equals(actual.getComponentsPatch()),
                    "Wool output must retain the complete authored name, Grade marker and nested custom data");
            helper.assertTrue(actual.getMaxStackSize() == 16 && !actual.has(DataComponents.RARITY),
                    "Wool output must preserve component overrides and removals, not just custom data");
            helper.assertTrue(same(original, authored) && same(donor, inputs.getStackInSlot(0)),
                    "Preparation must not mutate the authored output or retained sheep");

            ItemStack required = new ItemStack(expectedItem, 3);
            required.applyComponents(original.getComponentsPatch());
            TerrestrialHabitatRecipe downstream = recipe.create(ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "test_wool_consumer"),
                    new ModRecipeData(new IngredientItem[]{new IngredientItem(required, true)},
                            new OutputItem[0], new FluidStack[0], new FluidStack[0], 0));
            helper.assertTrue(!downstream.matchesAnimalInput(required, new ItemStack(expectedItem, 3)),
                    "Downstream fixture must require authored metadata, not merely wool color");
            helper.assertTrue(downstream.tryConsumeIngredients(destination, List.of()) && destination.getStackInSlot(0).isEmpty(),
                    "Reloaded wool must still satisfy the exact tagged downstream ingredient");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void tierTwoGrowthPreservesThreeIndividualsAcrossReload(GameTestHelper helper) {
        Fixture f = place(helper, false);
        List<ItemStack> babies = List.of(sheep(true, 14, "Red Lamb"), sheep(true, 11, "Blue Lamb"), sheep(true, 5, "Lime Lamb"));
        for (int slot = 0; slot < babies.size(); slot++) f.inputs.setStackInSlot(slot, babies.get(slot).copy());
        f.inputs.setStackInSlot(3, new ItemStack(ItemRegistries.SHEEP_FEED.get(), 10));
        f.water.getInternalTank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        f.energy.setEnergy(120000);
        tick(helper, f);
        helper.assertTrue(consumed(save(helper, f.machine)), "Tier-two habitat must consume inputs on its first processing tick");
        helper.assertTrue(count(f.inputs, ItemRegistries.BABY_SHEEP.get()) == 0 && f.inputs.getStackInSlot(3).isEmpty(), "All three babies and exactly ten feed must be consumed");
        helper.assertTrue(f.water.getInternalTank().getFluidInTank(0).isEmpty(), "Habitat must consume exactly 1000 mB water");
        f = reload(helper, f);
        finish(helper, f, ItemRegistries.SHEEP.get(), 3);
        List<CompoundTag> expected = babies.stream().map(CapturedAnimalStackState::forAdult).toList();
        List<CompoundTag> actual = animalStates(f.outputs, ItemRegistries.SHEEP.get());
        helper.assertTrue(actual.size() == 3 && actual.containsAll(expected), "Three adults must preserve each lamb's complete distinct payload, with no donor clones");
        helper.assertTrue(f.energy.getInternalEnergyStorage().getEnergyStored() == 0, "Reload must retain progress and consume exactly 120000 FE");
        helper.assertTrue(count(f.outputs, ItemRegistries.MANURE.get()) == 6, "Tier-two byproduct quantity must remain unchanged");
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void blockedOutputRejectsReplacementDonor(GameTestHelper helper) { blockedReplacement(helper, false); }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void blockedOutputRejectsReplacementDonorAfterReload(GameTestHelper helper) { blockedReplacement(helper, true); }

    private static void blockedReplacement(GameTestHelper helper, boolean withReload) {
        Fixture f = place(helper, false);
        ItemStack original = sheep(true, 14, "Original A");
        ItemStack replacement = sheep(true, 11, "Replacement B");
        f.inputs.setStackInSlot(0, original.copy());
        supplyTierOne(f, false);
        for (int slot = 0; slot < f.outputs.getSlots(); slot++) f.outputs.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, 64));
        tick(helper, f);
        CompoundTag blocked = save(helper, f.machine);
        helper.assertTrue(blocked.contains("activeRecipeSnapshot") && !consumed(blocked), "A blocked transaction must have a prepared snapshot but leave the animal unconsumed");
        helper.assertTrue(same(f.inputs.getStackInSlot(0), original), "Blocked preparation must not edit or consume the donor");
        if (withReload) f = reload(helper, f);
        ItemStack retained = f.inputs.extractItem(0, 1, false);
        f.inputs.setStackInSlot(0, replacement.copy());
        for (int slot = 0; slot < f.outputs.getSlots(); slot++) f.outputs.setStackInSlot(slot, ItemStack.EMPTY);
        tick(helper, f);
        helper.assertTrue(same(f.inputs.getStackInSlot(0), replacement), "The stale prepared transaction must reject a different individual's state before consuming it");
        helper.assertTrue(count(f.outputs, ItemRegistries.SHEEP.get()) == 0, "The rejected transaction must emit no original-donor copy");
        helper.assertTrue(!save(helper, f.machine).contains("activeRecipeSnapshot"), "The invalid binding must clear rather than remain stuck");
        finish(helper, f, ItemRegistries.SHEEP.get(), 1);
        helper.assertTrue(animalStates(f.outputs, ItemRegistries.SHEEP.get()).equals(List.of(CapturedAnimalStackState.forAdult(replacement))), "The next transaction must grow B, never emit retained A's payload");
        helper.assertTrue(same(retained, original), "The player's retained original must remain unchanged");
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void breedingPreservesBothParentsAcrossConsumedReload(GameTestHelper helper) {
        Fixture f = place(helper, true);
        ItemStack first = sheep(false, 14, "Parent A");
        ItemStack second = sheep(false, 11, "Parent B");
        f.inputs.setStackInSlot(0, first.copy());
        f.inputs.setStackInSlot(1, second.copy());
        supplyTierOne(f, true);
        tick(helper, f);
        helper.assertTrue(consumed(save(helper, f.machine)), "Breeding must have committed its consumable inputs before reload");
        helper.assertTrue(same(f.inputs.getStackInSlot(0), first) && same(f.inputs.getStackInSlot(1), second), "Both parent stacks must remain byte-for-byte unchanged");
        helper.assertTrue(f.inputs.getStackInSlot(3).isEmpty() && f.water.getInternalTank().getFluidInTank(0).isEmpty(), "Food and water must be consumed once before reload");
        f = reload(helper, f);
        finish(helper, f, ItemRegistries.BABY_SHEEP.get(), 1);
        helper.assertTrue(same(f.inputs.getStackInSlot(0), first) && same(f.inputs.getStackInSlot(1), second), "Completion after reload must preserve both parent payloads");
        helper.assertTrue(animalStates(f.outputs, ItemRegistries.BABY_SHEEP.get()).equals(List.of(CapturedAnimalStackState.forOffspring(first))), "Newborn must use the deterministic first parent and exclude individual name/inventory state");
        helper.assertTrue(f.energy.getInternalEnergyStorage().getEnergyStored() == 0, "Breeding progress must resume and use exactly 20000 FE");
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void legacySheepColorSurvivesHabitat(GameTestHelper helper) {
        Fixture f = place(helper, false);
        ItemStack legacy = legacySheep(true, 14);
        f.inputs.setStackInSlot(0, legacy);
        supplyTierOne(f, false);
        finish(helper, f, ItemRegistries.SHEEP.get(), 1);
        helper.assertTrue(animalStates(f.outputs, ItemRegistries.SHEEP.get()).equals(List.of(CapturedAnimalStackState.forAdult(legacySheep(true, 14)))), "SheepColor-only legacy lamb must keep its colour through actual habitat processing");
        assertRedSheepReconstruction(helper, f.outputs, ItemRegistries.SHEEP.get(), false);
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void legacySheepColorSurvivesBreeding(GameTestHelper helper) {
        Fixture f = place(helper, true);
        ItemStack first = legacySheep(false, 14);
        ItemStack second = legacySheep(false, 11);
        f.inputs.setStackInSlot(0, first.copy());
        f.inputs.setStackInSlot(1, second.copy());
        supplyTierOne(f, true);
        finish(helper, f, ItemRegistries.BABY_SHEEP.get(), 1);
        helper.assertTrue(animalStates(f.outputs, ItemRegistries.BABY_SHEEP.get()).equals(List.of(CapturedAnimalStackState.forOffspring(first))), "Legacy breeding must normalize SheepColor before variant inheritance");
        assertRedSheepReconstruction(helper, f.outputs, ItemRegistries.BABY_SHEEP.get(), true);
        helper.assertTrue(same(f.inputs.getStackInSlot(0), first) && same(f.inputs.getStackInSlot(1), second), "Legacy migration must not rewrite retained parent stacks");
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void probabilisticBatchRemainsOnePersistedRoll(GameTestHelper helper) {
        ItemStackHandler inputs = new ItemStackHandler(3);
        for (int slot = 0; slot < 3; slot++) inputs.setStackInSlot(slot, sheep(true, slot, "Distinct " + slot));
        TerrestrialHabitatRecipe recipe = new TerrestrialHabitatRecipe(ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "test_chance_batch"),
                new ModRecipeData(new IngredientItem[] {new IngredientItem(new ItemStack(ItemRegistries.BABY_SHEEP.get(), 3), true)},
                        new OutputItem[] {new OutputItem(new ItemStack(ItemRegistries.SHEEP.get(), 3), 0.5f)}, new FluidStack[0], new FluidStack[0], 0));
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        for (int trial = 0; trial < 64; trial++) {
            ModRecipeData prepared = AnimalRecipeStatePreparation.prepareGrowth(recipe, inputs).getRecipe();
            var encoded = ModRecipeData.CODEC.encodeStart(ops, prepared).result().orElseThrow();
            ModRecipeData restored = ModRecipeData.CODEC.parse(ops, encoded).result().orElseThrow();
            int animalCount = java.util.Arrays.stream(restored.getOutputItems()).mapToInt(output -> output.getItemStack().getCount()).sum();
            helper.assertTrue(animalCount == 0 || animalCount == 3, "A probabilistic count-three batch must remain all-or-none, not three independent rolls");
            if (animalCount == 3) helper.assertTrue(java.util.Arrays.stream(restored.getOutputItems())
                    .map(output -> CapturedAnimalStackState.read(output.getItemStack())).distinct().count() == 3,
                    "A successful chance batch must preserve all three distinct individual payloads");
            for (OutputItem output : restored.getOutputItems()) helper.assertTrue(output.getChance() == 1.0f, "Saved split outputs must have resolved chance decisions");
            TerrestrialHabitatRecipe reloaded = recipe.create(recipe.getId(), restored);
            helper.assertTrue(reloaded.tryConsumeIngredients(copyInputs(inputs), List.of()), "Persisted exact-state bindings must still accept their original inputs");
            ItemStackHandler changed = copyInputs(inputs);
            changed.setStackInSlot(1, sheep(true, 12, "Substitute"));
            helper.assertTrue(!reloaded.tryConsumeIngredients(changed, List.of()), "Persisted bindings must reject substitution in every member of a batch");
            helper.assertTrue(changed.getStackInSlot(0).getCount() == 1 && changed.getStackInSlot(2).getCount() == 1, "Failed binding validation must leave all other individuals unconsumed");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void largeIdenticalGrowthBatchFitsSnapshotCodec(GameTestHelper helper) {
        ItemStackHandler inputs = new ItemStackHandler(5);
        IngredientItem[] ingredients = new IngredientItem[5];
        OutputItem[] outputs = new OutputItem[5];
        for (int slot = 0; slot < 5; slot++) {
            ItemStack babies = sheep(true, 14, "Identical Legacy Batch");
            babies.setCount(64);
            inputs.setStackInSlot(slot, babies);
            ingredients[slot] = new IngredientItem(new ItemStack(ItemRegistries.BABY_SHEEP.get(), 64), true);
            outputs[slot] = new OutputItem(new ItemStack(ItemRegistries.SHEEP.get(), 64), 1.0f);
        }
        TerrestrialHabitatRecipe recipe = new TerrestrialHabitatRecipe(ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "test_large_identical_batch"),
                new ModRecipeData(ingredients, outputs, new FluidStack[0], new FluidStack[0], 0));
        ModRecipeData prepared = AnimalRecipeStatePreparation.prepareGrowth(recipe, inputs).getRecipe();
        helper.assertTrue(prepared.getOutputItems().length == 5, "320 identical adult outputs must aggregate to five legal stacks, not exceed the 256-entry snapshot limit");
        helper.assertTrue(java.util.Arrays.stream(prepared.getOutputItems()).mapToInt(output -> output.getItemStack().getCount()).sum() == 320,
                "Runtime aggregation must retain all 320 output individuals before encoding");
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var encoded = ModRecipeData.CODEC.encodeStart(ops, prepared).result().orElseThrow();
        ModRecipeData restored = ModRecipeData.CODEC.parse(ops, encoded).result().orElseThrow();
        helper.assertTrue(java.util.Arrays.stream(restored.getOutputItems()).mapToInt(output -> output.getItemStack().getCount()).sum() == 320,
                "Snapshot serialization templates must preserve the runtime aggregated output counts");
        helper.assertTrue(java.util.Arrays.stream(restored.getIngredientItems()).mapToInt(input -> input.getItemStack().getCount()).sum() == 320,
                "Snapshot serialization templates must preserve the runtime bound input counts");
        TerrestrialHabitatRecipe reloaded = recipe.create(recipe.getId(), restored);
        ItemStackHandler destination = new ItemStackHandler(5);
        helper.assertTrue(reloaded.tryConsumeIngredients(inputs, List.of()), "Large round-tripped transaction must consume its bound inputs");
        reloaded.assemble(destination, List.of());
        helper.assertTrue(count(inputs, ItemRegistries.BABY_SHEEP.get()) == 0 && count(destination, ItemRegistries.SHEEP.get()) == 320, "Large snapshot must consume and emit exactly 320 individuals");
        for (int slot = 0; slot < 5; slot++) helper.assertTrue(destination.getStackInSlot(slot).getCount() == 64, "Aggregated output stack counts must remain legal");
        ItemStackHandler fragmentedInputs = new ItemStackHandler(12);
        for (int slot = 0; slot < 12; slot++) {
            ItemStack batch = sheep(true, 14, "Fragmented Donor");
            batch.setCount(64);
            fragmentedInputs.setStackInSlot(slot, batch);
        }
        IngredientItem[] fragmentedRequirements = new IngredientItem[256];
        for (int index = 0; index < fragmentedRequirements.length; index++) fragmentedRequirements[index] =
                new IngredientItem(new ItemStack(ItemRegistries.BABY_SHEEP.get(), 3), true);
        TerrestrialHabitatRecipe fragmented = recipe.create(recipe.getId(), new ModRecipeData(
                fragmentedRequirements, new OutputItem[0], new FluidStack[0], new FluidStack[0], 0));
        ModRecipeData compact = AnimalRecipeStatePreparation.prepareGrowth(fragmented, fragmentedInputs).getRecipe();
        helper.assertTrue(compact.getIngredientItems().length == 12, "264 allocation fragments must coalesce into twelve legal same-state bindings");
        var compactTag = ModRecipeData.CODEC.encodeStart(ops, compact).result().orElseThrow();
        ModRecipeData compactReload = ModRecipeData.CODEC.parse(ops, compactTag).result().orElseThrow();
        helper.assertTrue(java.util.Arrays.stream(compactReload.getIngredientItems()).mapToInt(input -> input.getItemStack().getCount()).sum() == 768,
                "Coalesced input templates must encode all 768 individuals, including fragments merged after construction");
        helper.assertTrue(recipe.create(recipe.getId(), compactReload).tryConsumeIngredients(fragmentedInputs, List.of())
                        && count(fragmentedInputs, ItemRegistries.BABY_SHEEP.get()) == 0,
                "Bound input coalescing must preserve all 768 required individuals through serialization and consumption");
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void overlappingAnimalSelectorsFindCompleteAllocation(GameTestHelper helper) {
        ItemStackHandler inputs = new ItemStackHandler(2);
        ItemStack baby = capturedHorse(-1200, "Constrained Baby");
        ItemStack adult = capturedHorse(0, "Flexible Adult");
        inputs.setStackInSlot(0, baby.copy());
        inputs.setStackInSlot(1, adult.copy());
        TerrestrialHabitatRecipe recipe = overlapRecipe();
        helper.assertTrue(recipe.recipeMatch(inputs, List.of(), null, List.of()), "Fixture must pass provider input preflight");
        TerrestrialHabitatRecipe prepared = AnimalRecipeStatePreparation.prepareGrowth(recipe, inputs);
        helper.assertTrue(prepared.getItemIngredients().size() == 2, "Both concrete donors must be bound");
        helper.assertTrue(prepared.matchesAnimalInput(prepared.getItemIngredients().get(0).getItemStack(), adult)
                        && prepared.matchesAnimalInput(prepared.getItemIngredients().get(1).getItemStack(), baby),
                "Flexible first requirement must move to the adult so the constrained baby selector can be satisfied");
        helper.assertTrue(prepared.tryConsumeIngredients(inputs, List.of()) && inputs.getStackInSlot(0).isEmpty() && inputs.getStackInSlot(1).isEmpty(),
                "Complete overlapping allocation must consume exactly both selected individuals");
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 100)
    public static void impossibleAnimalSelectorsFailWithoutConsumption(GameTestHelper helper) {
        ItemStackHandler inputs = new ItemStackHandler(2);
        ItemStack first = capturedHorse(0, "Adult One");
        ItemStack second = capturedHorse(0, "Adult Two");
        inputs.setStackInSlot(0, first.copy());
        inputs.setStackInSlot(1, second.copy());
        TerrestrialHabitatRecipe recipe = overlapRecipe();
        helper.assertTrue(recipe.recipeMatch(inputs, List.of(), null, List.of()), "Regression fixture must exercise the provider's overlapping-selector false positive");
        boolean failedSafely = false;
        try {
            AnimalRecipeStatePreparation.prepareGrowth(recipe, inputs);
        } catch (com.nstut.nstutlib.recipes.RecipeTransactionException expected) {
            failedSafely = true;
        }
        helper.assertTrue(failedSafely, "Unsatisfiable preparation must throw only the controller's safely handled failure type");
        helper.assertTrue(same(inputs.getStackInSlot(0), first) && same(inputs.getStackInSlot(1), second),
                "Failed allocation must not mutate or consume any donor");
        sustainedPreparationRejections(helper);
        helper.succeed();
    }

    private static void sustainedPreparationRejections(GameTestHelper helper) {
        Fixture f = place(helper, false);
        for (boolean bindingLimit : new boolean[]{false, true}) {
            for (int slot = 0; slot < f.inputs.getSlots(); slot++) f.inputs.setStackInSlot(slot, ItemStack.EMPTY);
            f.inputs.setStackInSlot(0, capturedHorse(0, "Adult One"));
            f.inputs.setStackInSlot(1, capturedHorse(0, "Adult Two"));
            IngredientItem[] requirements = overlapRecipe().getRecipe().getIngredientItems();
            if (bindingLimit) {
                f.inputs.setStackInSlot(2, capturedHorse(0, "Adult Three"));
                requirements = new IngredientItem[256];
                ItemStack any = horseRequirement(CapturedAnimalItem.LIFECYCLE_ANY);
                any.setCount(3);
                requirements[0] = new IngredientItem(any, true);
                for (int i = 1; i < requirements.length; i++) requirements[i] = new IngredientItem(new ItemStack(Items.WHEAT), true);
                for (int slot = 3; slot < 7; slot++) f.inputs.setStackInSlot(slot, new ItemStack(Items.WHEAT, slot == 6 ? 63 : 64));
            }
            TerrestrialHabitatRecipe rejected = overlapRecipe().create(overlapRecipe().getId(),
                    new ModRecipeData(requirements, new OutputItem[0],
                            new FluidStack[]{new FluidStack(Fluids.WATER, 250)}, new FluidStack[0], 32000));
            f.water.setFluid(new FluidStack(Fluids.WATER, 250));
            f.energy.setEnergy(32000);
            helper.assertTrue(rejected.recipeMatch(f.inputs, List.of(f.water.getInternalTank()), null, List.of()),
                    "Native rejection fixture must pass actual provider input preflight");
            List<ItemStack> original = new ArrayList<>();
            for (int slot = 0; slot < f.inputs.getSlots(); slot++) original.add(f.inputs.getStackInSlot(slot).copy());
            withTestRecipe(helper, rejected, () -> {
                tick(helper, f);
                for (int attempt = 0; attempt < 3; attempt++) {
                    helper.assertTrue((int) machineField(f.machine, "processingFailureCooldown") == 20,
                            "Every allocation/binding rejection must enter the provider cooldown");
                    helper.assertTrue((boolean) machineField(f.machine, "isStructureValid")
                            && !save(helper, f.machine).contains("activeRecipeSnapshot"),
                            "Rejected preparation must preserve structure without installing a transaction");
                    for (int slot = 0; slot < original.size(); slot++) helper.assertTrue(same(original.get(slot), f.inputs.getStackInSlot(slot)),
                            "Sustained rejection must retain every original input");
                    helper.assertTrue(f.water.getInternalTank().getFluidInTank(0).getAmount() == 250
                            && f.energy.getInternalEnergyStorage().getEnergyStored() == 32000,
                            "Rejected preparation must not consume water or energy");
                    for (int remaining = 19; remaining >= 0; remaining--) {
                        tick(helper, f);
                        helper.assertTrue((int) machineField(f.machine, "processingFailureCooldown") == remaining,
                                "Unchanged invalid allocation/binding must not retry before cooldown expiration");
                    }
                    tick(helper, f);
                }
            });
            // Prove recovery through the real controller after a valid recipe replacement.
            ItemStack any = horseRequirement(CapturedAnimalItem.LIFECYCLE_ANY);
            TerrestrialHabitatRecipe recovery = rejected.create(rejected.getId(),
                    new ModRecipeData(new IngredientItem[]{new IngredientItem(any, true)}, new OutputItem[0],
                            new FluidStack[]{new FluidStack(Fluids.WATER, 250)}, new FluidStack[0], 32000));
            withTestRecipe(helper, recovery, () -> {
                for (int tick = 0; tick < 100; tick++) tick(helper, f);
                helper.assertTrue(f.inputs.getStackInSlot(0).isEmpty()
                        && f.water.getInternalTank().getFluidInTank(0).isEmpty()
                        && f.energy.getInternalEnergyStorage().getEnergyStored() == 0,
                        "A valid replacement must recover after bounded cooldown and charge resources once");
            });
        }
    }

    private static Object machineField(MachineBlockEntity machine, String name) {
        try {
            var field = MachineBlockEntity.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(machine);
        } catch (ReflectiveOperationException exception) { throw new AssertionError(exception); }
    }

    private static void withTestRecipe(GameTestHelper helper, TerrestrialHabitatRecipe recipe, Runnable test) {
        var manager = helper.getLevel().getServer().getRecipeManager();
        var original = List.copyOf(manager.getRecipes());
        try {
            manager.replaceRecipes(List.of(new net.minecraft.world.item.crafting.RecipeHolder<>(recipe.getId(), recipe)));
            test.run();
        } finally { manager.replaceRecipes(original); }
    }

    private static TerrestrialHabitatRecipe overlapRecipe() {
        return new TerrestrialHabitatRecipe(ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "test_overlapping_horse_selectors"),
                new ModRecipeData(new IngredientItem[] {
                        new IngredientItem(horseRequirement(CapturedAnimalItem.LIFECYCLE_ANY), true),
                        new IngredientItem(horseRequirement(CapturedAnimalItem.LIFECYCLE_BABY), true)},
                        new OutputItem[0], new FluidStack[0], new FluidStack[0], 0));
    }

    private static ItemStack horseRequirement(String lifecycle) {
        ItemStack stack = new ItemStack(ItemRegistries.CAPTURED_ANIMAL.get());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            root.putString(CapturedAnimalItem.ENTITY_TYPE_TAG, "minecraft:horse");
            root.putString(CapturedAnimalItem.RECIPE_LIFECYCLE_TAG, lifecycle);
        });
        return stack;
    }

    private static ItemStack capturedHorse(int age, String name) {
        ItemStack stack = new ItemStack(ItemRegistries.CAPTURED_ANIMAL.get());
        CompoundTag state = new CompoundTag();
        state.putInt("Age", age);
        state.putString("CustomName", name);
        CapturedAnimalStackState.writeCapture(stack, state, "minecraft:horse", -1);
        return stack;
    }

    private static void assertRedSheepReconstruction(GameTestHelper helper, IItemHandler inventory, Item item, boolean baby) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.is(item)) continue;
            CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            CompoundTag stored = root.getCompound("CapturedEntity");
            helper.assertTrue(stored.contains("Color") && stored.getByte("Color") == 14,
                    "Raw derived entity payload must independently contain red Color=14");
            Sheep reconstructed = (Sheep) ((MobItem) stack.getItem()).createMob(helper.getLevel(), stack);
            helper.assertTrue(reconstructed != null && reconstructed.getColor() == DyeColor.RED
                            && reconstructed.isBaby() == baby,
                    "Legacy red colour must reach the reconstructed entity, independently of the normalization helper used for expected NBT");
            return;
        }
        throw new AssertionError("Missing expected legacy sheep output");
    }

    private static ItemStackHandler copyInputs(IItemHandler source) {
        ItemStackHandler copy = new ItemStackHandler(source.getSlots());
        for (int slot = 0; slot < source.getSlots(); slot++) copy.setStackInSlot(slot, source.getStackInSlot(slot).copy());
        return copy;
    }

    private static ItemStack sheep(boolean baby, int color, String name) {
        ItemStack stack = new ItemStack(baby ? ItemRegistries.BABY_SHEEP.get() : ItemRegistries.SHEEP.get());
        CompoundTag state = new CompoundTag();
        state.putInt("Age", baby ? -1200 : 0);
        state.putByte("Color", (byte) color);
        state.putString("CustomName", name);
        state.putBoolean("Sheared", color % 2 == 0);
        CompoundTag customPayload = new CompoundTag();
        customPayload.putString("individual", name);
        customPayload.putInt("stored_items", color + 1);
        state.put("CustomPersistentPayload", customPayload);
        CapturedAnimalStackState.writeCapture(stack, state, "minecraft:sheep", color);
        return stack;
    }

    private static ItemStack legacySheep(boolean baby, int color) {
        ItemStack stack = new ItemStack(baby ? ItemRegistries.BABY_SHEEP.get() : ItemRegistries.SHEEP.get());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> root.putInt("SheepColor", color));
        return stack;
    }

    private static void supplyTierOne(Fixture f, boolean breeding) {
        f.inputs.setStackInSlot(3, new ItemStack(Items.WHEAT, breeding ? 2 : 4));
        f.water.getInternalTank().fill(new FluidStack(Fluids.WATER, breeding ? 200 : 400), IFluidHandler.FluidAction.EXECUTE);
        f.energy.setEnergy(breeding ? 20000 : 48000);
    }

    private static void finish(GameTestHelper helper, Fixture f, Item animal, int expected) {
        for (int tick = 0; tick < 260 && count(f.outputs, animal) < expected; tick++) tick(helper, f);
        helper.assertTrue(count(f.outputs, animal) == expected, "Actual livestock machine must finish with exactly " + expected + " animal outputs");
        helper.assertTrue(!save(helper, f.machine).contains("activeRecipeSnapshot"), "Finished transaction must clear its snapshot");
        helper.assertTrue(((com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity) f.machine).getDisplayRecipe() == null,
                "Completed reloaded cycle must clear the menu recipe view as well");
    }

    private static void tick(GameTestHelper helper, Fixture f) {
        BlockPos pos = f.machine.getBlockPos();
        MachineBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), f.machine);
    }

    private static CompoundTag save(GameTestHelper helper, MachineBlockEntity machine) { return machine.saveWithFullMetadata(helper.getLevel().registryAccess()); }
    private static boolean consumed(CompoundTag tag) { return tag.getBoolean("ingredientsConsumed"); }
    private static boolean same(ItemStack a, ItemStack b) { return a.getCount() == b.getCount() && ItemStack.isSameItemSameComponents(a, b); }

    private static Fixture reload(GameTestHelper helper, Fixture f) {
        CompoundTag saved = save(helper, f.machine);
        helper.assertTrue(saved.contains("activeRecipeSnapshot"), "Reload regression requires a real persisted active recipe definition");
        MachineBlockEntity restored = f.machine instanceof BreedingChamberBlockEntity
                ? new BreedingChamberBlockEntity(f.machine.getBlockPos(), f.machine.getBlockState())
                : new TerrestrialHabitatBlockEntity(f.machine.getBlockPos(), f.machine.getBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        restored.setLevel(helper.getLevel());
        helper.getLevel().setBlockEntity(restored);
        CompoundTag roundTrip = save(helper, restored);
        helper.assertTrue(saved.get("activeRecipeSnapshot").equals(roundTrip.get("activeRecipeSnapshot")), "Full prepared outputs and exact input bindings must survive actual controller save/load");
        helper.assertTrue(consumed(saved) == consumed(roundTrip), "Input consumption phase must survive reload");
        return new Fixture(restored, f.inputs, f.outputs, f.water, f.energy);
    }

    private static Fixture place(GameTestHelper helper, boolean breeding) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 7));
        BlockState controller = (breeding ? MachineRegistries.BREEDING_CHAMBER.block().get() : MachineRegistries.TERRESTRIAL_HABITAT.block().get())
                .defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
        MachineBlockEntity blueprint = breeding ? new BreedingChamberBlockEntity(pos, controller) : new TerrestrialHabitatBlockEntity(pos, controller);
        MultiblockBlock[][][] blocks = blueprint.getMultiblockPattern().getPattern();
        for (int y = 0; y < blocks.length; y++) {
            MultiblockBlock[][] layer = blocks[y];
            for (int z = 0; z < layer.length; z++) for (int x = 0; x < layer[z].length; x++) {
                MultiblockBlock expected = layer[z][x];
                if (expected == null) continue;
                BlockPos target = MultiblockPattern.rotateBlockPos(pos, blueprint.getSouthOffsetX(), blueprint.getSouthOffsetY(), blueprint.getSouthOffsetZ(), blocks.length, layer.length, x, y, z, controller);
                BlockState state = expected.getBlock().defaultBlockState();
                for (Map.Entry<String, String> property : expected.getStates().entrySet()) state = property(state, state.getBlock().getStateDefinition().getProperty(property.getKey()), property.getValue());
                helper.getLevel().setBlock(target, state, 3);
            }
        }
        MachineBlockEntity machine = require(helper, pos, MachineBlockEntity.class);
        helper.assertTrue(machine.checkMultiblock(helper.getLevel(), pos, controller), "Test must build a valid actual livestock multiblock");
        return new Fixture(machine,
                require(helper, pos.offset(-3, -1, -1), ItemInputHatchBlockEntity.class).getInternalItemStorage(),
                require(helper, pos.offset(3, -1, -3), ItemOutputHatchBlockEntity.class).getInternalItemStorage(),
                require(helper, pos.offset(-2, -1, -6), FluidInputHatchBlockEntity.class),
                require(helper, pos.offset(0, -1, -6), EnergyInputHatchBlockEntity.class));
    }

    private static <T extends Comparable<T>> BlockState property(BlockState state, Property<T> property, String value) {
        return state.setValue(property, property.getValue(value).orElseThrow());
    }
    private static <T extends BlockEntity> T require(GameTestHelper helper, BlockPos pos, Class<T> type) {
        return type.cast(helper.getLevel().getBlockEntity(pos));
    }
    private static int count(IItemHandler inventory, Item item) {
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) if (inventory.getStackInSlot(slot).is(item)) count += inventory.getStackInSlot(slot).getCount();
        return count;
    }
    private static List<CompoundTag> animalStates(IItemHandler inventory, Item item) {
        List<CompoundTag> states = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(item)) for (int count = 0; count < stack.getCount(); count++) states.add(CapturedAnimalStackState.read(stack));
        }
        return states;
    }
    private record Fixture(MachineBlockEntity machine, IItemHandlerModifiable inputs, IItemHandlerModifiable outputs, FluidInputHatchBlockEntity water, EnergyInputHatchBlockEntity energy) {}
}
