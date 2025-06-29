package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.machines.MachineType;
import com.nstut.biotech.views.machines.menu.GeneExtractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class GeneExtractorBlockEntity extends MachineBlockEntity {

    public GeneExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    protected Component getContainerName() {
        return Component.translatable("container.biotech.gene_extractor");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GeneExtractorMenu(id, inventory, this);
    }

    @Override
    public MachineType getMachineType() {
        return MachineType.GENE_EXTRACTOR;
    }
}