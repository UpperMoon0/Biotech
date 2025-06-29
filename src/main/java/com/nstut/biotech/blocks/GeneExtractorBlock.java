package com.nstut.biotech.blocks;

import com.nstut.biotech.blocks.entites.machines.GeneExtractorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GeneExtractorBlock extends Block implements EntityBlock {

    public GeneExtractorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneExtractorBlockEntity(pos, state);
    }
}