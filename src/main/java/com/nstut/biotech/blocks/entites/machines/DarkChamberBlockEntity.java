package com.nstut.biotech.blocks.entites.machines;

import com.nstut.biotech.machines.MachineType;
import com.nstut.biotech.views.machines.menu.DarkChamberMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DarkChamberBlockEntity extends MachineBlockEntity {

    public DarkChamberBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, MachineType.DARK_CHAMBER);
    }

    @Override
    protected Component get ,[`ContainerTitle()`](file:///media/nstut/Primary%20Storage/Workspaces/MC%20Mods/Tech/MC-Mod-Biotech/src/main/java/com/nstut/biotech/blocks/entites/machines/DarkChamberBlockEntity.java:16) {
        return Component.translatable("block.biotech.dark_chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DarkChamberMenu(id, inventory, this);
    }
}