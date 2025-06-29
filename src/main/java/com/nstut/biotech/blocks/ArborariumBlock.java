package com.nstut.biotech.blocks;

import com.nstut.biotech.blocks.entites.machines.ArborariumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ArborariumBlock extends Block implements EntityBlock {

    public ArborariumBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArborariumBlockEntity(pos, state);
    }
}