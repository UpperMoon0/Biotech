package com.nstut.biotech.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.nstut.biotech.Biotech;
import com.nstut.biotech.Config;
import com.nstut.biotech.blocks.entites.hatches.*;
import com.nstut.biotech.blocks.entites.machines.SlaughterhouseBlockEntity;
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
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Regression tests use real structures, hatches, loaded recipes and the production transaction tick. */
@GameTestHolder(Biotech.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LivestockProductionGameTests {
    private LivestockProductionGameTests() {}

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 400)
    public static void controllerDiagnosticsAndRedstonePreserveCycle(GameTestHelper helper) {
        Rig rig = slaughter(helper);
        var machine = (com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity) rig.machine;
        tick(helper, machine, 1);
        helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.MISSING_ITEMS, "Empty valid controller must report missing items");
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
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
        helper.assertTrue(machine.getMachineStatus() == com.nstut.biotech.machines.MachineStatus.MISSING_FLUID, "Matching cow without water must report missing fluid");
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

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 400)
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

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 400)
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

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 400)
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

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 400)
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

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 400)
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

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 400)
    public static void renewableMilkBlocksAndReloadsWithoutConsumingAdult(GameTestHelper helper) {
        helper.assertTrue(ForgeMod.MILK.get() != Fluids.EMPTY, "Biotech must explicitly enable loader milk on a minimal installation");
        Rig rig = habitat(helper);
        ItemStack donor = animal(ItemRegistries.COW.get(), "cow", -1, null);
        rig.animals.setStackInSlot(0, donor.copy());
        rig.food.setStackInSlot(0, new ItemStack(Items.WHEAT, 4));
        rig.water.setFluid(new FluidStack(Fluids.WATER, 500));
        rig.energy.setEnergy(128);
        for (FluidStack blocked : List.of(new FluidStack(Fluids.LAVA, 1), new FluidStack(ForgeMod.MILK.get(), rig.milk.getInternalTank().getTankCapacity(0)))) {
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
        helper.assertTrue(rig.milk.getInternalTank().getFluidInTank(0).getFluid() == ForgeMod.MILK.get() && rig.milk.getInternalTank().getFluidInTank(0).getAmount() == 2000, "Two loaded default recipes must deliver 2000 mB of registered milk through the actual fluid output hatch");
        helper.assertTrue(rig.food.getStackInSlot(0).isEmpty() && rig.water.getInternalTank().getFluidInTank(0).isEmpty() && rig.energy.getInternalEnergyStorage().getEnergyStored() == 0 && counts(rig.outputs).getOrDefault(ItemRegistries.MANURE.get(), 0) == 2, "Reloaded milk cycles must charge exact food, water and FE and emit manure once per cycle");
        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 400)
    public static void goatDatapackExampleDecodesAndRepeats(GameTestHelper helper) {
        ModRecipeData data;
        try (var stream = LivestockProductionGameTests.class.getResourceAsStream("/biotech_examples/goat_milk.json")) {
            if (stream == null) throw new AssertionError("Missing goat datapack example");
            data = ModRecipeData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(stream))).result().orElseThrow(() -> new AssertionError("Target-specific goat example must decode with the production recipe codec"));
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

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "livestock", timeoutTicks = 400)
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
        return (SlaughterhouseRecipe) helper.getLevel().getRecipeManager().byKey(id("slaughterhouse_" + animal)).orElseThrow();
    }

    private static ResourceLocation id(String path) { return new ResourceLocation(Biotech.MOD_ID, path); }
    private static boolean same(ItemStack a, ItemStack b) { return ItemStack.matches(a, b); }
    private static void encode(GameTestHelper helper, ModRecipeData data) {
        helper.assertTrue(ModRecipeData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().isPresent(), "Prepared output snapshot must encode successfully, including amplified counts");
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
        CompoundTag saved = old.saveWithFullMetadata();
        helper.assertTrue(saved.contains("activeRecipeSnapshot"), "In-flight recipe snapshot must be present on disk");
        BlockState state = helper.getLevel().getBlockState(old.getBlockPos());
        MachineBlockEntity reloaded = old instanceof SlaughterhouseBlockEntity ? new SlaughterhouseBlockEntity(old.getBlockPos(), state) : new TerrestrialHabitatBlockEntity(old.getBlockPos(), state);
        reloaded.load(saved);
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
    private static MachineBlockEntity place(GameTestHelper helper, boolean habitat) {
        BlockPos pos = helper.absolutePos(new BlockPos(8, 2, 8));
        BlockState controller = (habitat ? MachineRegistries.TERRESTRIAL_HABITAT : MachineRegistries.SLAUGHTERHOUSE).block().get().defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
        MachineBlockEntity blueprint = habitat ? new TerrestrialHabitatBlockEntity(pos, controller) : new SlaughterhouseBlockEntity(pos, controller);
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
