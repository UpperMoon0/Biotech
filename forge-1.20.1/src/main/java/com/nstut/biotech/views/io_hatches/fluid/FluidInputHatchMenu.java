package com.nstut.biotech.views.io_hatches.fluid;

import com.nstut.biotech.views.MenuRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class FluidInputHatchMenu extends FluidHatchMenu {
    public FluidInputHatchMenu(int pContainerId, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
        this(pContainerId, inventory, Objects.requireNonNull(inventory.player.level().getBlockEntity(friendlyByteBuf.readBlockPos())));
    }
    public FluidInputHatchMenu(int pContainerId, Inventory inventory, BlockEntity blockEntity) {
        super( MenuRegistries.FLUID_INPUT_HATCH.get(), pContainerId, inventory, blockEntity);
    }
}
