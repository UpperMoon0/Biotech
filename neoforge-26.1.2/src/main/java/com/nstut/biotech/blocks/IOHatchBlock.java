package com.nstut.biotech.blocks;

import com.mojang.serialization.MapCodec;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IOHatchBlock extends BaseEntityBlock {
    private final int type;
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public IOHatchBlock(BlockBehaviour.Properties properties, int type) {
        super(properties);
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return MapCodec.unit(this); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override protected @NotNull RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (type) {
            case 0 -> new ItemInputHatchBlockEntity(pos, state);
            case 1 -> new ItemOutputHatchBlockEntity(pos, state);
            case 2 -> new FluidInputHatchBlockEntity(pos, state);
            case 3 -> new FluidOutputHatchBlockEntity(pos, state);
            default -> new EnergyInputHatchBlockEntity(pos, state);
        };
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!level.getBlockState(pos).is(state.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ItemHatchBlockEntity itemHatch) itemHatch.dropItem();
            else if (blockEntity instanceof FluidHatchBlockEntity fluidHatch) fluidHatch.dropItem();
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof FluidHatchBlockEntity fluidHatch)
                || !FluidHatchBlockEntity.isFluidContainer(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!level.isClientSide()) {
            FluidUtil.interactWithFluidHandler(player, hand, pos, fluidHatch.getManualFluidStorage(), null);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CapabilityBlockEntity hatch) {
            serverPlayer.openMenu(hatch, buffer -> buffer.writeBlockPos(pos));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide()) {
            if (type == 2 || type == 3) return FluidHatchBlockEntity::serverTick;
            if (type == 4) return EnergyHatchBlockEntity::serverTick;
        }
        return null;
    }
}
