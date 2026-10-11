package com.nstut.biotech.gametest;

import com.nstut.biotech.Config;
import com.nstut.biotech.blocks.entites.machines.FermenterBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.*;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.machines.RedstoneMode;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;

/** A real disk-backed chunk unload/reload, not a reconstruction from an in-memory tag. */
public final class MachineChunkUpgradeChecks {
    private MachineChunkUpgradeChecks() {}
    public static void legacyActiveCycleSurvivesChunkUnload(GameTestHelper helper) {
        var level=helper.getLevel();
        // Far outside the GameTest structure and player/spawn tickets; entirely disposable test world.
        int cx=(helper.absolutePos(BlockPos.ZERO).getX()>>4)+4096;
        int cz=(helper.absolutePos(BlockPos.ZERO).getZ()>>4)+4096;
        BlockPos anchor=new BlockPos((cx<<4)+5,80,(cz<<4)+11);
        var state=MachineRegistries.FERMENTER.block().get().defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH);
        var blueprint=new FermenterBlockEntity(anchor,state);
        var blocks=blueprint.getLegacyMultiblockPattern().getPattern();
        List<BlockPos> positions=new ArrayList<>();
        level.setChunkForced(cx,cz,true);
        for(int y=0;y<blocks.length;y++)for(int z=0;z<blocks[y].length;z++)for(int x=0;x<blocks[y][z].length;x++){
            var part=blocks[y][z][x];if(part==null)continue;
            BlockPos at=MultiblockPattern.rotateBlockPos(anchor,blueprint.getSouthOffsetX(),blueprint.getSouthOffsetY(),blueprint.getSouthOffsetZ(),blocks.length,blocks[y].length,x,y,z,state);
            helper.assertTrue((at.getX()>>4)==cx&&(at.getZ()>>4)==cz,"Legacy upgrade fixture must fit one unloadable chunk");
            BlockState placed=part.getBlock().defaultBlockState();
            for(var entry:part.getStates().entrySet())placed=property(placed,entry.getKey(),entry.getValue());
            level.setBlock(at,placed,2);positions.add(at);
        }
        var machine=(FermenterBlockEntity)level.getBlockEntity(anchor);
        helper.assertTrue(machine.checkMultiblock(level,anchor,state),"Unchanged 2.5 Fermenter forms");
        var input=(ItemInputHatchBlockEntity)level.getBlockEntity(anchor.offset(-2,1,-5));
        var output=(ItemOutputHatchBlockEntity)level.getBlockEntity(anchor.offset(2,1,-5));
        var water=(FluidInputHatchBlockEntity)level.getBlockEntity(anchor.offset(-1,-1,-8));
        var energy=(EnergyInputHatchBlockEntity)level.getBlockEntity(anchor.offset(1,-1,-8));
        input.getInternalItemStorage().setStackInSlot(0,new ItemStack(ItemRegistries.PAPER_BAG.get(),16));
        input.getInternalItemStorage().setStackInSlot(1,new ItemStack(Items.DIRT));
        input.getInternalItemStorage().setStackInSlot(2,new ItemStack(Items.BONE_MEAL,3));
        input.getInternalItemStorage().setStackInSlot(3,new ItemStack(ItemRegistries.MANURE.get(),4));
        input.getInternalItemStorage().setStackInSlot(8,new ItemStack(Items.GOLD_INGOT,7));
        output.getInternalItemStorage().setStackInSlot(8,new ItemStack(Items.DIAMOND,2));
        water.setFluid(new FluidStack(Fluids.WATER,750));
        energy.setEnergy(128);
        int oldRate=Config.machineEnergyPerTick;double oldMultiplier=Config.machineEnergyMultiplier;
        try{Config.machineEnergyPerTick=128;Config.machineEnergyMultiplier=1.0;MachineBlockEntity.serverTick(level,anchor,state,machine);}
        finally{Config.machineEnergyPerTick=oldRate;Config.machineEnergyMultiplier=oldMultiplier;}
        machine.setRedstoneMode(RedstoneMode.HIGH); // No signal: pause while waiting for natural ticket expiry.
        energy.setEnergy(32);
        var savedMachine=machine.saveWithFullMetadata();
        var savedInput=input.saveWithFullMetadata();var savedOutput=output.saveWithFullMetadata();var savedWater=water.saveWithFullMetadata();var savedEnergy=energy.saveWithFullMetadata();
        helper.assertTrue(savedMachine.contains("activeRecipeSnapshot"),"Upgrade fixture has a real consumed active snapshot");
        level.getChunkSource().save(true);
        level.setChunkForced(cx,cz,false);
        boolean[] done={false};
        int[] releasedTicks={0};
        helper.onEachTick(()->{
            if(done[0]||level.getChunkSource().hasChunk(cx,cz))return;
            // Do not immediately revive a pending-unload chunk before its save future settles.
            if(++releasedTicks[0]<20)return;
            level.getChunkSource().save(true);
            done[0]=true;
            // hasChunk=false is the evidence that the actual provider released the chunk.
            level.setChunkForced(cx,cz,true);level.getChunk(cx,cz);
            try{
                var restored=(FermenterBlockEntity)level.getBlockEntity(anchor);
                var restoredInput=(ItemInputHatchBlockEntity)level.getBlockEntity(input.getBlockPos());
                var restoredOutput=(ItemOutputHatchBlockEntity)level.getBlockEntity(output.getBlockPos());
                var restoredWater=(FluidInputHatchBlockEntity)level.getBlockEntity(water.getBlockPos());
                var restoredEnergy=(EnergyInputHatchBlockEntity)level.getBlockEntity(energy.getBlockPos());
                helper.assertTrue(restored!=null&&restored!=machine,"Chunk load creates a fresh controller from persisted world data");
                helper.assertTrue(savedMachine.equals(restored.saveWithFullMetadata()),"Active snapshot, progress, rolls and redstone mode survive real chunk unload/load exactly");
                helper.assertTrue(savedInput.equals(restoredInput.saveWithFullMetadata())&&savedOutput.equals(restoredOutput.saveWithFullMetadata())&&savedWater.equals(restoredWater.saveWithFullMetadata())&&savedEnergy.equals(restoredEnergy.saveWithFullMetadata()),"Populated item, fluid and FE hatches survive real chunk unload/load exactly");
                helper.assertTrue(restored.checkMultiblock(level,anchor,level.getBlockState(anchor)),"Loaded 2.5 fixture still forms without rebuilding");
                restored.setRedstoneMode(RedstoneMode.IGNORE);restoredEnergy.setEnergy(32000-128);
                for(int tick=0;tick<1000;tick++)MachineBlockEntity.serverTick(level,anchor,level.getBlockState(anchor),restored);
                int fertilizer=0,diamonds=0;
                for(int slot=0;slot<restoredOutput.getInternalItemStorage().getSlots();slot++){
                    var stack=restoredOutput.getInternalItemStorage().getStackInSlot(slot);
                    if(stack.is(ItemRegistries.FERTILIZER.get()))fertilizer+=stack.getCount();
                    if(stack.is(Items.DIAMOND))diamonds+=stack.getCount();
                }
                helper.assertTrue(fertilizer==16&&diamonds==2,"Reloaded cycle emits exactly one saved result and retains preexisting output");
                helper.assertTrue(restoredInput.getInternalItemStorage().getStackInSlot(8).getCount()==7&&restoredWater.getInternalTank().getFluidInTank(0).getAmount()==250&&restoredEnergy.getInternalEnergyStorage().getEnergyStored()==0,"No duplicate consumption of stored items, water or FE");
                for(BlockPos at:positions)level.setBlock(at,Blocks.AIR.defaultBlockState(),2);
                helper.succeed();
            }finally{level.setChunkForced(cx,cz,false);}
        });
    }
    private static <T extends Comparable<T>> BlockState property(BlockState state,String name,String value){@SuppressWarnings("unchecked") Property<T> p=(Property<T>)state.getBlock().getStateDefinition().getProperty(name);return state.setValue(p,p.getValue(value).orElseThrow());}
}
