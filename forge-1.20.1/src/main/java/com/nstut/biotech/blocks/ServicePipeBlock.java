package com.nstut.biotech.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;

public final class ServicePipeBlock extends ServicePipeBlockBase {
    public ServicePipeBlock(Properties properties){super(properties);}
    @Override public BlockState updateShape(BlockState state,Direction direction,BlockState neighbour,LevelAccessor level,BlockPos pos,BlockPos neighbourPos){
        return state.setValue(connection(direction),neighbour.getBlock() instanceof ServicePipeBlockBase);
    }
}
