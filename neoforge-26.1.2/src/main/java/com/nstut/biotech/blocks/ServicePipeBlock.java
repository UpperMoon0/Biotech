package com.nstut.biotech.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;

public final class ServicePipeBlock extends ServicePipeBlockBase {
    public ServicePipeBlock(Properties properties){super(properties);}
    @Override public BlockState updateShape(BlockState state,LevelReader level,ScheduledTickAccess ticks,BlockPos pos,Direction direction,BlockPos neighbourPos,BlockState neighbour,RandomSource random){
        return state.setValue(connection(direction),neighbour.getBlock() instanceof ServicePipeBlockBase);
    }
}
