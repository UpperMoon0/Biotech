package com.nstut.biotech.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Decorative vertical service conduit, with collision matching the authored model. */
public final class ServicePipeBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(Block.box(5, 0, 5, 11, 16, 11),
            Block.box(3, 0, 3, 13, 2, 13), Block.box(3, 14, 3, 13, 16, 13));
    public ServicePipeBlock(Properties properties) { super(properties); }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
