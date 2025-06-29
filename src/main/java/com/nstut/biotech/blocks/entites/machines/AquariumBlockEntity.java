package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.machines.MachineType;
import com.nstut.biotech.views.machines.menu.AquariumMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AquariumBlockEntity extends MachineBlockEntity {

    public AquariumBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    protected Component getContainerTitle() {
        return Component.translatable("container.biotech.aquarium");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AquariumMenu(id, inventory, this);
    }

    @Override
    public MachineType getMachineType() {
        return MachineType.AQUARIUM;
    }
}