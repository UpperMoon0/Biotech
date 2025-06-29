package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.machines.MachineType;
import com.nstut.biotech.views.machines.menu.ArborariumMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ArborariumBlockEntity extends MachineBlockEntity {

    public ArborariumBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    protected Component getContainerTitle() {
        return Component.translatable("container.biotech.arborarium");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ArborariumMenu(id, inventory, this);
    }

    @Override
    public MachineType getMachineType() {
        return MachineType.ARBORARIUM;
    }
}