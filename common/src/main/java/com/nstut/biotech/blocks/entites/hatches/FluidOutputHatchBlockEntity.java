package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.entites.BlockEntityRegistries;
import com.nstut.biotech.views.io_hatches.fluid.FluidOutputHatchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidOutputHatchBlockEntity extends FluidHatchBlockEntity {
    public FluidOutputHatchBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistries.FLUID_OUTPUT_HATCH.get(), pos, state);
    }
    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.title.biotech.fluid_output_hatch");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new FluidOutputHatchMenu(pContainerId, pPlayerInventory, this);
    }
}
