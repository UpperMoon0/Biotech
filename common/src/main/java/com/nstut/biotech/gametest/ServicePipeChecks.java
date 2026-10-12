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
    public static void oldPaletteRepair(GameTestHelper helper) {
        var level=helper.getLevel();var pipe=BlockRegistries.BIOTECH_PIPE.get();
        var serialized=com.google.gson.JsonParser.parseString("{\"Name\":\"biotech:biotech_pipe\"}");
        var old=net.minecraft.world.level.block.state.BlockState.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,serialized)
                .result().orElseThrow();
        helper.assertTrue(old.is(pipe),"Pre-2.7 name-only palette resolves to registered pipe");
        int cx=(helper.absolutePos(BlockPos.ZERO).getX()>>4)+6144;
        int cz=(helper.absolutePos(BlockPos.ZERO).getZ()>>4)+6144;
        var left=new BlockPos((cx<<4)+15,80,(cz<<4)+8);var right=left.east();
        level.setChunkForced(cx,cz,true);level.setChunkForced(cx+1,cz,true);
        var originalLeft=level.getChunk(cx,cz);var originalRight=level.getChunk(cx+1,cz);
        int[] stage={0}, ticks={0}, unloadedTicks={0};
        helper.onEachTick(()->{
            if(stage[0]==0) {
                // Let initial load callbacks drain before writing the old saved palette fixture.
                if(++ticks[0]<5)return;
                for(var pos:new BlockPos[]{left,left.above(),right,right.above()}) {
                    var chunk=level.getChunkAt(pos);
                    chunk.getSection(chunk.getSectionIndex(pos.getY())).setBlockState(pos.getX()&15,pos.getY()&15,pos.getZ()&15,old);
                    chunk.setUnsaved(true);
                }
                helper.assertTrue(!level.getBlockState(left).getValue(ServicePipeBlockBase.EAST),"Saved old run starts disconnected");
                level.getChunkSource().save(true);
                level.setChunkForced(cx,cz,false);level.setChunkForced(cx+1,cz,false);stage[0]=1;return;
            }
            if(stage[0]==1) {
                if(level.getChunkSource().hasChunk(cx,cz)||level.getChunkSource().hasChunk(cx+1,cz))return;
                if(++unloadedTicks[0]<20)return;
                level.getChunkSource().save(true);
                helper.assertTrue(level.getChunk(cx,cz)!=originalLeft,"First chunk reloaded from disk");
                ticks[0]=0;stage[0]=2;return;
            }
            if(stage[0]==2) {
                if(++ticks[0]<3)return;
                helper.assertTrue(level.getBlockState(left).getValue(ServicePipeBlockBase.UP),"Real chunk load hook reconnects vertical pipe");
                helper.assertTrue(!level.getChunkSource().hasChunk(cx+1,cz),"Repair never force-loads neighboring chunk");
                helper.assertTrue(level.getChunk(cx+1,cz)!=originalRight,"Second chunk reloaded from disk");
                ticks[0]=0;stage[0]=3;return;
            }
            if(stage[0]==3) {
                if(++ticks[0]<3)return;
                try {
                    helper.assertTrue(level.getBlockState(left).getValue(ServicePipeBlockBase.EAST)
                            &&level.getBlockState(right).getValue(ServicePipeBlockBase.WEST),"Later neighboring load repairs both sides of chunk edge");
                    helper.assertTrue(level.getBlockState(right).getValue(ServicePipeBlockBase.UP)
                            &&level.getBlockState(right.above()).getValue(ServicePipeBlockBase.DOWN),"Vertical connections restored too");
                    for(var pos:new BlockPos[]{left,left.above(),right,right.above()})level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
                    stage[0]=4;helper.succeed();
                } finally {level.setChunkForced(cx,cz,false);level.setChunkForced(cx+1,cz,false);}
            }
        });
    }
    public static void hammerResourceSafety(GameTestHelper helper,net.minecraft.world.entity.player.Player player) {
        var level=helper.getLevel();var target=helper.absolutePos(new BlockPos(4,4,4));
        level.setBlock(target.below(),Blocks.STONE.defaultBlockState(),3);
        player.getAbilities().mayBuild=true;player.getAbilities().instabuild=false;
        var states=new net.minecraft.world.level.block.state.BlockState[]{
                Blocks.OAK_SLAB.defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.DOUBLE),
                Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES,4),
                Blocks.SEA_PICKLE.defaultBlockState().setValue(net.minecraft.world.level.block.SeaPickleBlock.PICKLES,4),
                Blocks.POTTED_POPPY.defaultBlockState(),Blocks.CANDLE_CAKE.defaultBlockState()};
        for(var state:states) {
            level.setBlock(target,state,3);player.getInventory().clearContent();
            player.getInventory().add(new net.minecraft.world.item.ItemStack(Blocks.BRICKS));
            helper.assertTrue(!hammerCell(helper,player,target,new com.nstut.nstutlib.models.MultiblockBlock(Blocks.BRICKS)),"Resource-bearing replacement fails closed");
            helper.assertTrue(level.getBlockState(target).equals(state)&&itemCount(player,Blocks.BRICKS.asItem())==1,"Blocked replacement leaves resources and inventory intact");
            if(state.is(Blocks.OAK_SLAB)||state.is(Blocks.CANDLE)||state.is(Blocks.SEA_PICKLE)) {
                var correction=new com.nstut.nstutlib.models.MultiblockBlock(state.getBlock(),java.util.Map.of(
                        state.is(Blocks.OAK_SLAB)?"type":state.is(Blocks.CANDLE)?"candles":"pickles",state.is(Blocks.OAK_SLAB)?"bottom":"1"));
                helper.assertTrue(!hammerCell(helper,player,target,correction)&&level.getBlockState(target).equals(state),"Same-block quantity reduction fails closed");
            }
        }
        level.setBlock(target,Blocks.OAK_SLAB.defaultBlockState(),3);player.getInventory().clearContent();
        helper.assertTrue(!hammerCell(helper,player,target,new com.nstut.nstutlib.models.MultiblockBlock(Blocks.OAK_SLAB,java.util.Map.of("type","double"))),"Same-block increase cannot duplicate slab items");
        level.setBlock(target,Blocks.AIR.defaultBlockState(),3);
        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
        helper.assertTrue(hammerCell(helper,player,target,new com.nstut.nstutlib.models.MultiblockBlock(Blocks.WATER)),"Water cells remain constructible with one water bucket");
        helper.assertTrue(level.getBlockState(target).is(Blocks.WATER)&&itemCount(player,net.minecraft.world.item.Items.WATER_BUCKET)==0
                &&itemCount(player,net.minecraft.world.item.Items.BUCKET)==1,"Water placement preserves the empty bucket exactly once");
        helper.succeed();
    }
    private static int itemCount(net.minecraft.world.entity.player.Player player,net.minecraft.world.item.Item item) {
        int count=0;for(int slot=0;slot<player.getInventory().getContainerSize();slot++) {
            var stack=player.getInventory().getItem(slot);if(stack.is(item))count+=stack.getCount();
        }return count;
    }
    private static boolean hammerCell(GameTestHelper helper,net.minecraft.world.entity.player.Player player,BlockPos target,
            com.nstut.nstutlib.models.MultiblockBlock cell) {
        var anchor=target.west();helper.getLevel().setBlock(anchor,Blocks.FURNACE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH),3);
        var pattern=new com.nstut.nstutlib.models.MultiblockPattern(new com.nstut.nstutlib.models.MultiblockBlock[][][]{{{cell}}});
        try {
            var hammer=(com.nstut.nstutlib.items.SmartHammer)com.nstut.nstutlib.items.ItemRegistries.SMART_HAMMER.get();
            var build=com.nstut.nstutlib.items.SmartHammer.class.getDeclaredMethod("buildStructure",net.minecraft.world.level.Level.class,
                    net.minecraft.world.entity.player.Player.class,BlockPos.class,com.nstut.nstutlib.models.MultiblockPattern.class,int.class,int.class,int.class);
            build.setAccessible(true);return (boolean)build.invoke(hammer,helper.getLevel(),player,anchor,pattern,-1,0,0);
        }catch(ReflectiveOperationException exception){throw new AssertionError("Unable to invoke hammer fixture",exception);}
    }
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
