package com.nstut.biotech.gametest;

import com.nstut.biotech.Config;
import com.nstut.biotech.blocks.entites.hatches.*;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.machines.MachineStructures;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.fluids.FluidStack;
import java.lang.reflect.Field;
import java.util.*;

/** Real loaded recipes + placed structures + production serverTick, on the current required layout. */
public final class MachineLifecycleChecks {
    private MachineLifecycleChecks() {}

    public static void allMachineCycles(GameTestHelper helper) {
        int oldRate = Config.machineEnergyPerTick;
        double oldMultiplier = Config.machineEnergyMultiplier;
        Config.machineEnergyPerTick = 128;
        Config.machineEnergyMultiplier = 1.0;
        try {
            for (String id : MachineStructures.ids()) for (int layout = 0; layout < 1; layout++) for (boolean customFluids : new boolean[] {false,true}) {
                if(customFluids&&!id.equals("mixer"))continue;
                List<BlockPos> touched = new ArrayList<>();
                try { runCycle(helper, id, layout, touched, customFluids); }
                finally { for (BlockPos pos : touched) helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 2); }
            }
        } finally { Config.machineEnergyPerTick = oldRate; Config.machineEnergyMultiplier = oldMultiplier; }
        helper.succeed();
    }

    private static void runCycle(GameTestHelper helper, String id, int layout, List<BlockPos> touched, boolean customFluids) {
        boolean legacy = layout == 1;
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(16, 2, 16));
        String context = id + (layout == 2 ? "/2.6 upgrade" : legacy ? "/2.5 upgrade" : "/redesigned") + (customFluids ? "/saved three-fluid datapack cycle" : "");
        int[] origin = MachineStructures.origin(id);
        MultiblockPattern pattern = MachineStructures.pattern(id);
        var array = pattern.getPattern();
        var controller = array[array.length-origin[1]-1][array[0].length-origin[2]-1][origin[0]].getBlock()
                .defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
        level.setBlock(pos, controller, 2); touched.add(pos);
        MachineBlockEntity machine = (MachineBlockEntity) level.getBlockEntity(pos);
        try {
            if (legacy) pattern = (MultiblockPattern) machine.getClass().getMethod("getLegacyMultiblockPattern").invoke(machine);
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
        if (layout == 2) pattern = MachineStructures.habitat26Pattern();
        array = pattern.getPattern();
        List<BlockPos> hatchPositions = new ArrayList<>();
        BlockPos shell = null;
        for (int y=0; y<array.length; y++) for (int z=0; z<array[y].length; z++) for (int x=0; x<array[y][z].length; x++) {
            MultiblockBlock part = array[y][z][x];
            if (part == null) continue;
            BlockPos target = MultiblockPattern.rotateBlockPos(pos, origin[0], origin[1], origin[2], array.length, array[y].length, x, y, z, controller);
            BlockState state = part.getBlock().defaultBlockState();
            for (var entry : part.getStates().entrySet()) state = property(state, entry.getKey(), entry.getValue());
            level.setBlock(target, state, 2); touched.add(target);
            if (!target.equals(pos) && level.getBlockEntity(target) != null) hatchPositions.add(target);
            if (shell == null && !target.equals(pos) && !state.isAir() && level.getBlockEntity(target) == null) shell = target;
        }
        helper.assertTrue(machine.checkMultiblock(level, pos, controller), context + " forms before processing");
        String recipeName = switch (id) {
            case "greenhouse" -> "greenhouse_wheat";
            case "breeding_chamber" -> "breeding_chamber_cow_t1_wheat";
            case "terrestrial_habitat" -> "terrestrial_habitat_cow_milk_t1_wheat";
            case "slaughterhouse" -> "slaughterhouse_cow";
            case "mixer" -> "chicken_feed";
            case "fermenter" -> "fertilizer";
            default -> throw new AssertionError(id);
        };
        ModRecipe<?> recipe = (ModRecipe<?>) level.recipeAccess().byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,net.minecraft.resources.Identifier.fromNamespaceAndPath("biotech",recipeName))).orElseThrow().value();
        // Stock Mixer recipes have no fluids. This additional persisted datapack snapshot
        // exercises all existing fluid ports without adding a gameplay recipe to the mod.
        if(customFluids) recipe=new com.nstut.biotech.recipes.MixerRecipe(recipe.getId(),new ModRecipeData(
            new com.nstut.nstutlib.recipes.IngredientItem[]{new com.nstut.nstutlib.recipes.IngredientItem(new ItemStack(Items.PAPER),true)},
            new com.nstut.nstutlib.recipes.OutputItem[]{new com.nstut.nstutlib.recipes.OutputItem(new ItemStack(Items.DIAMOND),1.0f)},
            new FluidStack[]{new FluidStack(net.minecraft.world.level.material.Fluids.WATER,100),new FluidStack(net.minecraft.world.level.material.Fluids.LAVA,200),new FluidStack(net.neoforged.neoforge.common.NeoForgeMod.MILK.get(),300)},
            new FluidStack[]{new FluidStack(net.minecraft.world.level.material.Fluids.WATER,400)},1024));
        ModRecipeData data = recipe.getRecipe();
        List<ItemInputHatchBlockEntity> inputs = hatches(level, hatchPositions, ItemInputHatchBlockEntity.class);
        var outputs = hatches(level, hatchPositions, ItemOutputHatchBlockEntity.class).get(0).getInternalItemStorage();
        List<FluidInputHatchBlockEntity> fluids = hatches(level, hatchPositions, FluidInputHatchBlockEntity.class);
        EnergyInputHatchBlockEntity energy = hatches(level, hatchPositions, EnergyInputHatchBlockEntity.class).get(0);
        // Fill the actual facing-side capability, not the controller's private fields.
        int slot = 0;
        List<ItemStack> originalInputs = new ArrayList<>();
        for (var ingredient : data.getIngredientItems()) {
            ItemStack stack = ingredient.getItemStack().copy();
            if (CapturedAnimalStackState.entityTypeId(stack).equals("minecraft:cow")) {
                CompoundTag state = new CompoundTag(); state.putInt("Age", 0); state.putString("UpgradeFixtureMarker", context);
                CapturedAnimalStackState.writeCapture(stack, state, "minecraft:cow", -1);
            }
            originalInputs.add(stack.copy());
            var input = inputs.get(slot / 9);
            var facing = input.getBlockState().getValue(MachineBlock.FACING);
            var nativeCapability = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,input.getBlockPos(),facing);
            var capability = nativeCapability == null ? null : net.neoforged.neoforge.items.IItemHandler.of(nativeCapability);
            helper.assertTrue(capability != null && capability.insertItem(slot % 9, stack, false).isEmpty(), context + " accepts ingredient through exposed side");
            slot++;
        }
        for (int i=0; i<data.getFluidIngredients().length; i++) {
            var input = fluids.get(i);
            var facing = input.getBlockState().getValue(MachineBlock.FACING);
            var nativeCapability = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Fluid.BLOCK,input.getBlockPos(),facing);
            var capability = nativeCapability == null ? null : net.neoforged.neoforge.fluids.capability.IFluidHandler.of(nativeCapability);
            FluidStack fluid = data.getFluidIngredients()[i].copy();
            helper.assertTrue(capability != null && capability.fill(fluid, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE)==fluid.getAmount(), context + " accepts fluid through exposed side");
        }
        var facing = energy.getBlockState().getValue(MachineBlock.FACING);
        var power = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK,energy.getBlockPos(),facing);
        helper.assertTrue(power != null, context + " exposes FE capability");
        try (var transaction = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            helper.assertTrue(power.insert(128, transaction)==128, context + " accepts FE through exposed side");
            transaction.commit();
        }
        for (int i=0; i<outputs.getSlots(); i++) outputs.setStackInSlot(i,new ItemStack(Items.COBBLESTONE,64));
        if(customFluids){
            setField(machine,"activeRecipeKey",net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,net.minecraft.resources.Identifier.fromNamespaceAndPath("biotech",recipeName)));setField(machine,"activeRecipeSnapshot",data.copy());
            setField(machine,"activeItemOutputIndexes",new int[]{0});setField(machine,"recipeEnergyCost",1024);
        }
        tick(helper,machine,1);
        ModRecipeData prepared = (ModRecipeData) field(machine,"activeRecipeSnapshot");
        helper.assertTrue(prepared != null, context + " prepares a real loaded recipe while output is blocked");
        int[] rolls = ((int[])field(machine,"activeItemOutputIndexes")).clone();
        List<ItemStack> expected = new ArrayList<>();
        for (int index : rolls) expected.add(prepared.getOutputItems()[index].getItemStack().copy());
        helper.assertTrue((int)field(machine,"energyConsumed")==0 && energy.getInternalEnergyStorage().getEnergyStored()==128, context + " blocked output uses no FE");
        for (int i=0;i<originalInputs.size();i++) helper.assertTrue(ItemStack.matches(originalInputs.get(i),inputs.get(i/9).getInternalItemStorage().getStackInSlot(i%9)), context + " blocked output retains exact input payloads");
        // Save/recreate the controller AND every populated hatch, as chunk BE loading does.
        machine=reloadStored(helper,hatchPositions,pos,context);
        helper.assertTrue(Arrays.equals(rolls,(int[])field(machine,"activeItemOutputIndexes")),context+" saved output decisions survive reload");
        inputs = hatches(level,hatchPositions,ItemInputHatchBlockEntity.class);
        fluids = hatches(level,hatchPositions,FluidInputHatchBlockEntity.class);
        energy = hatches(level,hatchPositions,EnergyInputHatchBlockEntity.class).get(0);
        outputs = hatches(level,hatchPositions,ItemOutputHatchBlockEntity.class).get(0).getInternalItemStorage();
        for(int i=0;i<outputs.getSlots();i++)outputs.setStackInSlot(i,ItemStack.EMPTY);
        if(customFluids){
            var fluidOutput=hatches(level,hatchPositions,FluidOutputHatchBlockEntity.class).get(0);
            for(FluidStack obstruction:List.of(new FluidStack(net.minecraft.world.level.material.Fluids.LAVA,100),new FluidStack(net.minecraft.world.level.material.Fluids.WATER,32000))){
                fluidOutput.setFluid(obstruction);tick(helper,machine,2);
                helper.assertTrue((int)field(machine,"energyConsumed")==0 && energy.getInternalEnergyStorage().getEnergyStored()==128,context+" wrong/full fluid output does not spend FE");
                helper.assertTrue(inputs.get(0).getInternalItemStorage().getStackInSlot(0).getCount()==1,context+" wrong/full fluid output does not consume input");
                helper.assertTrue(fluids.stream().mapToInt(h->h.getInternalTank().getFluidInTank(0).getAmount()).sum()==600,context+" all three input fluids retained while fluid output blocked");
                helper.assertTrue(fluidOutput.getInternalTank().getFluidInTank(0).getAmount()==obstruction.getAmount(),context+" obstruction is not overwritten");
            }
            fluidOutput.setFluid(FluidStack.EMPTY);
        }
        tick(helper,machine,1);
        helper.assertTrue((int)field(machine,"energyConsumed")==128,context+" resumes exact saved cycle through the production tick");
        BlockState originalShell=level.getBlockState(shell);
        level.setBlock(shell,Blocks.AIR.defaultBlockState(),2);machine.requestStructureValidation();tick(helper,machine,2);
        helper.assertTrue(!(boolean)field(machine,"isStructureValid")&&(int)field(machine,"energyConsumed")==128,context+" invalidation pauses without resetting progress");
        helper.assertTrue(Arrays.equals(rolls,(int[])field(machine,"activeItemOutputIndexes")),context+" invalidation does not reroll");
        machine=reloadStored(helper,hatchPositions,pos,context+" consumed active cycle");
        helper.assertTrue((int)field(machine,"energyConsumed")==128 && Arrays.equals(rolls,(int[])field(machine,"activeItemOutputIndexes")),context+" active progress and decisions survive reload while invalid");
        inputs=hatches(level,hatchPositions,ItemInputHatchBlockEntity.class);
        fluids=hatches(level,hatchPositions,FluidInputHatchBlockEntity.class);
        energy=hatches(level,hatchPositions,EnergyInputHatchBlockEntity.class).get(0);
        outputs=hatches(level,hatchPositions,ItemOutputHatchBlockEntity.class).get(0).getInternalItemStorage();
        level.setBlock(shell,originalShell,2);machine.requestStructureValidation();
        energy.setEnergy(prepared.getTotalEnergy()-128);
        tick(helper,machine,prepared.getTotalEnergy()/128+5);
        assertExactOutputs(helper,outputs,expected,context);
        helper.assertTrue(energy.getInternalEnergyStorage().getEnergyStored()==0,context+" exact one-cycle FE cost");
        for(var fluid:fluids)helper.assertTrue(fluid.getInternalTank().getFluidInTank(0).isEmpty(),context+" fluids consumed once");
        for(int i=0;i<data.getIngredientItems().length;i++){
            ItemStack actual=inputs.get(i/9).getInternalItemStorage().getStackInSlot(i%9);
            helper.assertTrue(data.getIngredientItems()[i].isConsumable()?actual.isEmpty():ItemStack.matches(originalInputs.get(i),actual),context+" consumables consumed once and catalysts/animal state retained");
        }
        var fluidOutputs=hatches(level,hatchPositions,FluidOutputHatchBlockEntity.class);
        for(int i=0;i<prepared.getFluidOutputs().length;i++) {
            var expectedFluid=prepared.getFluidOutputs()[i];
            var actualFluid=fluidOutputs.get(i).getInternalTank().getFluidInTank(0);
            helper.assertTrue(actualFluid.getFluid()==expectedFluid.getFluid() && actualFluid.getAmount()==expectedFluid.getAmount(),context+" exact one-cycle fluid output");
        }
        tick(helper,machine,3);assertExactOutputs(helper,outputs,expected,context+" no replay");
        for(int i=0;i<prepared.getFluidOutputs().length;i++)helper.assertTrue(fluidOutputs.get(i).getInternalTank().getFluidInTank(0).getAmount()==prepared.getFluidOutputs()[i].getAmount(),context+" no fluid output replay");
        var outputHatch=hatches(level,hatchPositions,ItemOutputHatchBlockEntity.class).get(0);
        for(int i=0;i<outputs.getSlots();i++){
            ItemStack stack=outputs.getStackInSlot(i).copy();if(stack.isEmpty())continue;
            var extracted=extractSide(level,outputHatch,i,stack.getCount());
            helper.assertTrue(ItemStack.matches(stack,extracted),context+" facing-side item output extraction preserves exact payload");
        }
        for(int i=0;i<prepared.getFluidOutputs().length;i++){
            var expectedFluid=prepared.getFluidOutputs()[i];var drained=drainSide(level,fluidOutputs.get(i),expectedFluid.getAmount());
            helper.assertTrue(drained.getFluid()==expectedFluid.getFluid()&&drained.getAmount()==expectedFluid.getAmount(),context+" facing-side fluid output extraction works");
        }
        tick(helper,machine,3);
        for(int i=0;i<outputs.getSlots();i++)helper.assertTrue(outputs.getStackInSlot(i).isEmpty(),context+" extracted item outputs do not replay");
        for(var hatch:fluidOutputs)helper.assertTrue(hatch.getInternalTank().getFluidInTank(0).isEmpty(),context+" extracted fluids do not replay");
    }
    private static ItemStack extractSide(net.minecraft.world.level.Level level,ItemOutputHatchBlockEntity hatch,int slot,int amount){
        var side=hatch.getBlockState().getValue(MachineBlock.FACING);var capability=net.neoforged.neoforge.items.IItemHandler.of(level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,hatch.getBlockPos(),side));
        return capability.extractItem(slot,amount,false);
    }
    private static FluidStack drainSide(net.minecraft.world.level.Level level,FluidOutputHatchBlockEntity hatch,int amount){
        var side=hatch.getBlockState().getValue(MachineBlock.FACING);var capability=net.neoforged.neoforge.fluids.capability.IFluidHandler.of(level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Fluid.BLOCK,hatch.getBlockPos(),side));
        return capability.drain(amount,net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
    }
    private static void setField(MachineBlockEntity machine,String name,Object value){try{Field f=MachineBlockEntity.class.getDeclaredField(name);f.setAccessible(true);f.set(machine,value);}catch(ReflectiveOperationException ex){throw new AssertionError(ex);}}
    private static MachineBlockEntity reloadStored(GameTestHelper helper,List<BlockPos> hatchPositions,BlockPos pos,String context) {
        var level=helper.getLevel();
        List<BlockPos> persisted = new ArrayList<>(hatchPositions); persisted.add(pos);
        for (BlockPos at : persisted) {
            BlockEntity old = level.getBlockEntity(at);
            CompoundTag saved = old.saveWithFullMetadata(level.registryAccess());
            try {
                BlockEntity restored = old.getClass().getConstructor(BlockPos.class,BlockState.class).newInstance(at,old.getBlockState());
                restored.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),saved));
                level.setBlockEntity(restored);
                helper.assertTrue(saved.equals(restored.saveWithFullMetadata(level.registryAccess())), context + " exact persisted block entity round trip: " + old.getClass().getSimpleName());
            } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
        }
        return (MachineBlockEntity)level.getBlockEntity(pos);
    }
    private static void assertExactOutputs(GameTestHelper helper,IItemHandlerModifiable actual,List<ItemStack> expected,String context){
        List<ItemStack> remaining=new ArrayList<>();for(var stack:expected)remaining.add(stack.copy());
        for(int slot=0;slot<actual.getSlots();slot++){
            ItemStack stack=actual.getStackInSlot(slot);if(stack.isEmpty())continue;int left=stack.getCount();
            for(var candidate:remaining){if(candidate.isEmpty())continue;ItemStack a=stack.copy(),b=candidate.copy();a.setCount(1);b.setCount(1);if(ItemStack.matches(a,b)){int used=Math.min(left,candidate.getCount());candidate.shrink(used);left-=used;}}
            helper.assertTrue(left==0,context+" unexpected or duplicated output payload");
        }
        helper.assertTrue(remaining.stream().allMatch(ItemStack::isEmpty),context+" no missing saved output payload");
    }
    private static <T extends BlockEntity> List<T> hatches(net.minecraft.world.level.Level level,List<BlockPos> positions,Class<T> type){
        List<T> result=new ArrayList<>();for(var pos:positions){var be=level.getBlockEntity(pos);if(type.isInstance(be))result.add(type.cast(be));}return result;
    }
    private static void tick(GameTestHelper helper,MachineBlockEntity machine,int count){for(int i=0;i<count;i++)MachineBlockEntity.serverTick(helper.getLevel(),machine.getBlockPos(),helper.getLevel().getBlockState(machine.getBlockPos()),machine);}
    private static Object field(MachineBlockEntity machine,String name){try{Field f=MachineBlockEntity.class.getDeclaredField(name);f.setAccessible(true);return f.get(machine);}catch(ReflectiveOperationException ex){throw new AssertionError(ex);}}
    private static <T extends Comparable<T>> BlockState property(BlockState state,String name,String value){@SuppressWarnings("unchecked") Property<T> p=(Property<T>)state.getBlock().getStateDefinition().getProperty(name);return state.setValue(p,p.getValue(value).orElseThrow());}
}
