package com.nstut.biotech.blocks.entites;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base block entity for Biotech hatches.
 *
 * Capability implementations own their persistence. Keeping persistence in the
 * concrete hatch avoids unsafe casts from Forge capability interfaces to one
 * particular storage implementation.
 */
public abstract class CapabilityBlockEntity extends BlockEntity implements MenuProvider {
    protected CapabilityBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
