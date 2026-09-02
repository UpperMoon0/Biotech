package com.nstut.biotech.blocks;

import com.nstut.biotech.blocks.entites.hatches.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IOHatchBlock extends BaseEntityBlock {
    private final int type;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public IOHatchBlock(int type) {
        super(BlockBehaviour.Properties.copy(Blocks.GRAY_CONCRETE).strength(2f).sound(SoundType.METAL));
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(FACING);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    public @NotNull RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return switch (type) {
            case 0 -> new ItemInputHatchBlockEntity(pPos, pState);
            case 1 -> new ItemOutputHatchBlockEntity(pPos, pState);
            case 2 -> new FluidInputHatchBlockEntity(pPos, pState);
            case 3 -> new FluidOutputHatchBlockEntity(pPos, pState);
            default -> new EnergyInputHatchBlockEntity(pPos, pState);
        };
    }
    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof ItemHatchBlockEntity)
            {
                ((ItemHatchBlockEntity) blockEntity).dropItem();
            }
            if (blockEntity instanceof FluidHatchBlockEntity)
            {
                ((FluidHatchBlockEntity) blockEntity).dropItem();
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            switch (type) {
                case 0:
                    if (blockEntity instanceof ItemInputHatchBlockEntity)
                        NetworkHooks.openScreen((ServerPlayer) player, (ItemInputHatchBlockEntity) blockEntity, pos);
                    break;
                case 1:
                    if (blockEntity instanceof ItemOutputHatchBlockEntity)
                        NetworkHooks.openScreen((ServerPlayer) player, (ItemOutputHatchBlockEntity) blockEntity, pos);
                    break;
                case 2:
                    if (blockEntity instanceof FluidInputHatchBlockEntity)
                        NetworkHooks.openScreen((ServerPlayer) player, (FluidInputHatchBlockEntity) blockEntity, pos);
                    break;
                case 3:
                    if (blockEntity instanceof FluidOutputHatchBlockEntity)
                        NetworkHooks.openScreen((ServerPlayer) player, (FluidOutputHatchBlockEntity) blockEntity, pos);
                    break;
                default:
                    if (blockEntity instanceof EnergyInputHatchBlockEntity)
                        NetworkHooks.openScreen((ServerPlayer) player, (EnergyInputHatchBlockEntity) blockEntity, pos);
                    break;
            }
            return InteractionResult.CONSUME;
        } else
            return InteractionResult.SUCCESS;
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (!pLevel.isClientSide)
        {
            if (type == 2 || type == 3)
                return FluidHatchBlockEntity::serverTick;
            else if (type == 4)
                return EnergyHatchBlockEntity::serverTick;
        }
        return null;
    }
}
