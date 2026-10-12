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
            helper.assertTrue(state.getShape(level,center).bounds().minX()>=0&&state.getShape(level,center).bounds().maxX()<=1,"Collision stays inside its cell");
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
