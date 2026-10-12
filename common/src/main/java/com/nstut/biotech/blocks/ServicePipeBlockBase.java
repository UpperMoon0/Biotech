package com.nstut.biotech.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Six-direction decorative service duct. Connections are derived from adjacent pipes. */
public abstract class ServicePipeBlockBase extends Block {
    public static final BooleanProperty NORTH=BlockStateProperties.NORTH, SOUTH=BlockStateProperties.SOUTH,
            EAST=BlockStateProperties.EAST, WEST=BlockStateProperties.WEST, UP=BlockStateProperties.UP, DOWN=BlockStateProperties.DOWN;
    private static final VoxelShape[] SHAPES=new VoxelShape[64];
    static {
        VoxelShape hub=Block.box(4,4,4,12,12,12);
        VoxelShape[] arms={
            Shapes.or(Block.box(5,0,5,11,4,11),Block.box(3,0,3,13,2,13)),
            Shapes.or(Block.box(5,12,5,11,16,11),Block.box(3,14,3,13,16,13)),
            Shapes.or(Block.box(5,5,0,11,11,4),Block.box(3,3,0,13,13,2)),
            Shapes.or(Block.box(5,5,12,11,11,16),Block.box(3,3,14,13,13,16)),
            Shapes.or(Block.box(0,5,5,4,11,11),Block.box(0,3,3,2,13,13)),
            Shapes.or(Block.box(12,5,5,16,11,11),Block.box(14,3,3,16,13,13))
        };
        for(int mask=0;mask<64;mask++){
            VoxelShape shape=hub;
            for(Direction direction:Direction.values())if((mask&(1<<direction.ordinal()))!=0)shape=Shapes.or(shape,arms[direction.ordinal()]);
            SHAPES[mask]=shape.optimize();
        }
    }
    protected ServicePipeBlockBase(Properties properties){
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH,false).setValue(SOUTH,false).setValue(EAST,false)
                .setValue(WEST,false).setValue(UP,false).setValue(DOWN,false));
    }
    public static BooleanProperty connection(Direction direction){
        return switch(direction){case NORTH->NORTH;case SOUTH->SOUTH;case EAST->EAST;case WEST->WEST;case UP->UP;case DOWN->DOWN;};
    }
    public static BlockState connectedState(BlockState state,BlockGetter level,BlockPos pos){
        for(Direction direction:Direction.values())state=state.setValue(connection(direction),
                level.getBlockState(pos.relative(direction)).getBlock() instanceof ServicePipeBlockBase);
        return state;
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(NORTH,SOUTH,EAST,WEST,UP,DOWN);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){
        return connectedState(defaultBlockState(),context.getLevel(),context.getClickedPos());
    }
    @Override public void onPlace(BlockState state,Level level,BlockPos pos,BlockState previous,boolean moving){
        // Property-only repair must not query unloaded neighbors or recurse into another repair.
        if (previous.is(state.getBlock())) return;
        // Also handles direct setBlock callers such as Smart Hammer and structure placement.
        BlockState connected=connectedState(state,level,pos);
        if(connected!=state)level.setBlock(pos,connected,3);
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        int mask=0;
        for(Direction direction:Direction.values())if(state.getValue(connection(direction)))mask|=1<<direction.ordinal();
        return SHAPES[mask];
    }
    @Override public BlockState rotate(BlockState state,Rotation rotation){
        BlockState rotated=state;
        for(Direction direction:Direction.values())rotated=rotated.setValue(connection(rotation.rotate(direction)),state.getValue(connection(direction)));
        return rotated;
    }
    @Override public BlockState mirror(BlockState state,Mirror mirror){
        BlockState mirrored=state;
        for(Direction direction:Direction.values())mirrored=mirrored.setValue(connection(mirror.mirror(direction)),state.getValue(connection(direction)));
        return mirrored;
    }
}
