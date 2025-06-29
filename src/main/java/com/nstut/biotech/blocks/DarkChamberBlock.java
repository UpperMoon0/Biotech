package com.nstut.biotech.blocks;

import com.nstut.biotech.blocks.entites.machines.DarkChamberBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DarkChamberBlock extends Block implements EntityBlock {

    public DarkChamberBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DarkChamberBlockEntity(pos, state);
    }
}