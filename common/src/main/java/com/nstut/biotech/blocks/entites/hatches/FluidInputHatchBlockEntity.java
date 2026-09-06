package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.entites.BlockEntityRegistries;
import com.nstut.biotech.views.io_hatches.fluid.FluidInputHatchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FluidInputHatchBlockEntity extends FluidHatchBlockEntity {
    public FluidInputHatchBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistries.FLUID_INPUT_HATCH.get(), pos, state);
    }

    @Override
    protected boolean isInputHatch() {
        return true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("menu.title.biotech.fluid_input_hatch");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new FluidInputHatchMenu(containerId, inventory, this);
    }
}
