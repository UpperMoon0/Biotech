package com.nstut.biotech.gametest;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.ServicePipeBlockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;

public final class ServicePipeChecks {
    private ServicePipeChecks(){}
    public static void hammerBuild(GameTestHelper helper,net.minecraft.world.entity.player.Player player){
        var level=helper.getLevel();
        var anchor=helper.absolutePos(new BlockPos(4,4,4));
        var first=anchor.east();
        var pipe=BlockRegistries.BIOTECH_PIPE.get();
        level.setBlock(anchor,Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH),3);
        level.setBlock(first.north(),pipe.defaultBlockState(),3);
        level.setBlock(first,Blocks.AIR.defaultBlockState(),3);
        level.setBlock(first.east(),Blocks.AIR.defaultBlockState(),3);
        player.getInventory().clearContent();
        player.getAbilities().mayBuild=true;player.getAbilities().instabuild=false;
        player.getInventory().add(new net.minecraft.world.item.ItemStack(pipe,2));
        var cell=new com.nstut.nstutlib.models.MultiblockBlock(pipe);
        var pattern=new com.nstut.nstutlib.models.MultiblockPattern(new com.nstut.nstutlib.models.MultiblockBlock[][][]{{{cell,cell}}});
        try {
            var hammer=(com.nstut.nstutlib.items.SmartHammer)com.nstut.nstutlib.items.ItemRegistries.SMART_HAMMER.get();
            var build=com.nstut.nstutlib.items.SmartHammer.class.getDeclaredMethod("buildStructure",net.minecraft.world.level.Level.class,
                    net.minecraft.world.entity.player.Player.class,BlockPos.class,com.nstut.nstutlib.models.MultiblockPattern.class,int.class,int.class,int.class);
            build.setAccessible(true);
            helper.assertTrue((boolean)build.invoke(hammer,level,player,anchor,pattern,-1,0,0),"Smart Hammer builds pipes with available inventory");
        }catch(ReflectiveOperationException exception){throw new AssertionError("Unable to invoke hammer fixture",exception);}
        var state=level.getBlockState(first);
        helper.assertTrue(state.getValue(ServicePipeBlockBase.NORTH)&&state.getValue(ServicePipeBlockBase.EAST),"First pipe joins existing neighbour and newly built pipe");
        helper.assertTrue(level.getBlockState(first.north()).getValue(ServicePipeBlockBase.SOUTH)
                &&level.getBlockState(first.east()).getValue(ServicePipeBlockBase.WEST),"All connections are reciprocal");
        int remaining=0;
        for(int slot=0;slot<player.getInventory().getContainerSize();slot++){
            var stack=player.getInventory().getItem(slot);
            if(stack.is(pipe.asItem()))remaining+=stack.getCount();
        }
        helper.assertTrue(remaining==0,"Exactly two pipes consumed");
        helper.succeed();
    }
    public static void connectionsAndShapes(GameTestHelper helper){
        var level=helper.getLevel();
        var center=helper.absolutePos(new BlockPos(4,4,4));
        var pipe=BlockRegistries.BIOTECH_PIPE.get();
        // All 64 branch combinations, including straight runs, elbows, tees and six-way junctions.
        for(int mask=0;mask<64;mask++){
            for(Direction direction:Direction.values())level.setBlock(center.relative(direction),Blocks.AIR.defaultBlockState(),3);
            level.setBlock(center,pipe.defaultBlockState(),3);
            for(Direction direction:Direction.values())if((mask&(1<<direction.ordinal()))!=0)
                level.setBlock(center.relative(direction),pipe.defaultBlockState(),3);
            var state=level.getBlockState(center);
            helper.assertTrue(state.getShape(level,center).bounds().minX>=0&&state.getShape(level,center).bounds().maxX<=1,"Collision stays inside its cell");
            for(Direction direction:Direction.values()){
                boolean connected=(mask&(1<<direction.ordinal()))!=0;
                helper.assertTrue(state.getValue(ServicePipeBlockBase.connection(direction))==connected,"Center matches neighbour in "+direction+" for mask "+mask);
                if(connected)helper.assertTrue(level.getBlockState(center.relative(direction)).getValue(ServicePipeBlockBase.connection(direction.getOpposite())),"Neighbour connects back");
                double x=.5+direction.getStepX()*.49,y=.5+direction.getStepY()*.49,z=.5+direction.getStepZ()*.49;
                boolean arm=!Shapes.joinIsNotEmpty(state.getShape(level,center),Shapes.box(x-.005,y-.005,z-.005,x+.005,y+.005,z+.005),net.minecraft.world.phys.shapes.BooleanOp.ONLY_SECOND);
                helper.assertTrue(arm==connected,"Collision arm follows connection "+direction);
            }
        }
        for(Direction direction:Direction.values())level.setBlock(center.relative(direction),Blocks.STONE.defaultBlockState(),3);
        var state=level.getBlockState(center);
        for(Direction direction:Direction.values())helper.assertTrue(!state.getValue(ServicePipeBlockBase.connection(direction)),"Normal blocks do not create pipe connections");
        helper.succeed();
    }
}
