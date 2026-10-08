package com.nstut.biotech.items;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.NetTrapBlock;
import com.nstut.biotech.recipes.AnimalRecipeStatePreparation;
import com.nstut.biotech.recipes.BreedingChamberRecipe;
import com.nstut.biotech.recipes.SlaughterhouseRecipe;
import com.nstut.biotech.recipes.SlaughterhouseLootPreparation;
import com.nstut.biotech.recipes.TerrestrialHabitatRecipe;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Biotech.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MobItemStateGameTests {
    private MobItemStateGameTests() {
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void sanitizerPreservesGameplayStateWithoutWorldIdentity(GameTestHelper helper) {
        CompoundTag source = new CompoundTag();
        source.putString("CustomName", "Bessie");
        source.putInt("Age", -24000);
        source.putUUID("UUID", UUID.randomUUID());
        source.putString("Pos", "old-pos");
        source.putString("Motion", "old-motion");
        source.putString("Rotation", "old-rotation");
        source.putFloat("FallDistance", 4.0F);
        source.putInt("PortalCooldown", 40);
        source.putString("Leash", "old-leash");

        CompoundTag sanitized = CapturedEntityState.sanitize(source);

        helper.assertTrue("Bessie".equals(sanitized.getString("CustomName")) && sanitized.getInt("Age") == -24000,
                "Gameplay state must survive capture sanitization");
        helper.assertTrue(!sanitized.contains("UUID") && !sanitized.contains("Pos") && !sanitized.contains("Motion")
                        && !sanitized.contains("Rotation") && !sanitized.contains("FallDistance")
                        && !sanitized.contains("PortalCooldown") && !sanitized.contains("Leash"),
                "World identity and transform state must be stripped before release");
        helper.assertTrue(source.contains("UUID") && source.contains("Pos") && source.contains("Leash"),
                "Sanitizing a captured payload must not mutate the original tag");
        helper.assertTrue("CapturedEntity".equals(NetTrapBlock.CAPTURED_ENTITY_TAG),
                "Captured entity payload key must remain migration-compatible");

        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void captureStorageRoundTripsSanitizedState(GameTestHelper helper) {
        CompoundTag source = new CompoundTag();
        source.putString("CustomName", "Bessie");
        source.putInt("Age", -1200);
        source.putUUID("UUID", UUID.randomUUID());
        source.putString("Pos", "old-pos");
        source.putString("Motion", "old-motion");
        source.putString("Leash", "old-leash");

        ItemStack captured = new ItemStack(Items.PAPER);
        CapturedAnimalStackState.writeCapture(captured, source, "minecraft:cow", -1);

        CompoundTag root = captured.getTag();
        helper.assertTrue(root != null && root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG),
                "Capture must write an entity payload to the item");
        CompoundTag stored = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG);
        helper.assertTrue("Bessie".equals(stored.getString("CustomName")) && stored.getInt("Age") == -1200,
                "Persistent entity state must survive capture storage");
        helper.assertTrue(!stored.contains("UUID") && !stored.contains("Pos")
                        && !stored.contains("Motion") && !stored.contains("Leash"),
                "Transient world identity must be removed before the payload is stored");
        helper.assertTrue("minecraft:cow".equals(root.getString(CapturedAnimalItem.ENTITY_TYPE_TAG)),
                "Capture metadata must survive alongside the sanitized payload");
        helper.assertTrue(stored.equals(CapturedAnimalStackState.read(captured)),
                "Reading the captured stack must round-trip the already-sanitized stored state");
        helper.assertTrue(source.contains("UUID") && source.contains("Pos"),
                "Writing a capture must not mutate the entity's source NBT");
        helper.succeed();
    }
    @GameTest(templateNamespace = Biotech.MOD_ID, template = "empty", timeoutTicks = 40)
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
    @GameTest(templateNamespace = Biotech.MOD_ID, template = "empty", timeoutTicks = 40)
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

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "empty", timeoutTicks = 40)
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
                new ResourceLocation(Biotech.MOD_ID, "gametest_stateful_breeding"),
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
                new ResourceLocation(Biotech.MOD_ID, "gametest_stateful_growth"),
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

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void slaughterLootAndRenewableHabitatUseCapturedState(GameTestHelper helper) {
        ItemStack redSheep = new ItemStack(ItemRegistries.SHEEP.get());
        CompoundTag sheepState = new CompoundTag();
        sheepState.putInt("Age", 0);
        sheepState.putByte("Color", (byte) 14);
        CapturedAnimalStackState.writeCapture(redSheep, sheepState, "minecraft:sheep", 14);

        ItemStackHandler sheepInputs = new ItemStackHandler(1);
        sheepInputs.setStackInSlot(0, redSheep.copy());
        SlaughterhouseRecipe sheepSlaughter = new SlaughterhouseRecipe(
                new ResourceLocation(Biotech.MOD_ID, "gametest_red_sheep_loot"),
                new ModRecipeData(
                        new IngredientItem[] {new IngredientItem(new ItemStack(ItemRegistries.SHEEP.get()), true)},
                        new OutputItem[0], new FluidStack[0], new FluidStack[0], 0));
        SlaughterhouseRecipe preparedSheep = SlaughterhouseLootPreparation.prepare(
                sheepSlaughter, sheepInputs, helper.getLevel(), helper.absolutePos(new net.minecraft.core.BlockPos(0, 0, 0)));
        helper.assertTrue(preparedSheep.getItemOutputs().stream()
                        .anyMatch(output -> output.getItemStack().is(Items.RED_WOOL)
                                && output.getItemStack().getCount() == SlaughterhouseLootPreparation.YIELD_MULTIPLIER),
                "Colored sheep loot must come from the restored concrete entity and receive the 2x machine yield");
        helper.assertTrue(preparedSheep.getItemOutputs().stream().allMatch(output -> output.getChance() == 1.0f),
                "Loot-table RNG must be resolved before the transaction snapshot instead of becoming a second output roll");

        ItemStack cow = new ItemStack(ItemRegistries.COW.get());
        CompoundTag cowState = new CompoundTag();
        cowState.putInt("Age", 0);
        CapturedAnimalStackState.writeCapture(cow, cowState, "minecraft:cow", -1);
        ItemStackHandler cowInputs = new ItemStackHandler(1);
        cowInputs.setStackInSlot(0, cow);
        SlaughterhouseRecipe cowSlaughter = new SlaughterhouseRecipe(
                new ResourceLocation(Biotech.MOD_ID, "gametest_cow_loot"),
                new ModRecipeData(
                        new IngredientItem[] {new IngredientItem(new ItemStack(ItemRegistries.COW.get()), true)},
                        new OutputItem[0], new FluidStack[0], new FluidStack[0], 0));
        SlaughterhouseRecipe preparedCow = SlaughterhouseLootPreparation.prepare(
                cowSlaughter, cowInputs, helper.getLevel(), helper.absolutePos(new net.minecraft.core.BlockPos(0, 0, 0)));
        helper.assertTrue(!preparedCow.getItemOutputs().isEmpty(),
                "Cow Slaughterhouse processing must resolve its entity loot table");

        TerrestrialHabitatRecipe woolRecipe = new TerrestrialHabitatRecipe(
                new ResourceLocation(Biotech.MOD_ID, "gametest_renewable_wool"),
                new ModRecipeData(
                        new IngredientItem[] {new IngredientItem(new ItemStack(ItemRegistries.SHEEP.get()), false)},
                        new OutputItem[] {new OutputItem(new ItemStack(Items.WHITE_WOOL), 1.0f)},
                        new FluidStack[0], new FluidStack[0], 0));
        TerrestrialHabitatRecipe preparedWool = AnimalRecipeStatePreparation.prepareHabitat(woolRecipe, sheepInputs);
        helper.assertTrue(preparedWool.getItemOutputs().get(0).getItemStack().is(Items.RED_WOOL),
                "Renewable sheep production must preserve captured wool color");
        helper.assertTrue(sheepInputs.getStackInSlot(0).getCount() == 1,
                "Preparing renewable production must not consume or mutate the adult animal");

        TerrestrialHabitatRecipe milkRecipe = new TerrestrialHabitatRecipe(
                new ResourceLocation(Biotech.MOD_ID, "gametest_renewable_milk"),
                new ModRecipeData(
                        new IngredientItem[] {new IngredientItem(new ItemStack(ItemRegistries.COW.get()), false)},
                        new OutputItem[] {new OutputItem(new ItemStack(ItemRegistries.MANURE.get()), 1.0f)},
                        new FluidStack[0],
                        new FluidStack[] {new FluidStack(net.minecraftforge.common.ForgeMod.MILK.get(), 1000)},
                        0));
        TerrestrialHabitatRecipe preparedMilk = AnimalRecipeStatePreparation.prepareHabitat(milkRecipe, cowInputs);
        helper.assertTrue(!preparedMilk.getItemIngredients().get(0).isConsumable()
                        && preparedMilk.getFluidOutputs().get(0).getAmount() == 1000,
                "Renewable cow production must retain the adult catalyst and expose milk as fluid output");
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void genericCapturedSpeciesRecipeMatchesLifecycleSelectors(GameTestHelper helper) {
        BreedingChamberRecipe breeding = new BreedingChamberRecipe(
                new ResourceLocation(Biotech.MOD_ID, "gametest_generic_horse_adult"),
                genericSpeciesRecipeData(genericRequirement(EntityType.HORSE, CapturedAnimalItem.LIFECYCLE_ADULT)));
        TerrestrialHabitatRecipe habitat = new TerrestrialHabitatRecipe(
                new ResourceLocation(Biotech.MOD_ID, "gametest_generic_horse_baby"),
                genericSpeciesRecipeData(genericRequirement(EntityType.HORSE, CapturedAnimalItem.LIFECYCLE_BABY)));
        SlaughterhouseRecipe slaughter = new SlaughterhouseRecipe(
                new ResourceLocation(Biotech.MOD_ID, "gametest_generic_horse_any"),
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
        CompoundTag root = stack.getOrCreateTag();
        root.putString(CapturedAnimalItem.ENTITY_TYPE_TAG, EntityType.getKey(type).toString());
        root.putString(CapturedAnimalItem.RECIPE_LIFECYCLE_TAG, lifecycle);
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

}
