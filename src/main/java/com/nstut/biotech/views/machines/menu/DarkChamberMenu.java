package com.nstut.biotech.views.machines.menu;

import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.blocks.entites.machines.DarkChamberBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DarkChamberMenu extends AbstractContainerMenu {
    public final DarkChamberBlockEntity blockEntity;
    private final Level level;

    public DarkChamberMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public DarkChamberMenu(int id, Inventory inv, BlockEntity entity) {
        super(MachineRegistries.DARK_CHAMBER.getMenuType(), id);
        blockEntity = ((DarkChamberBlockEntity) entity);
        this.level = inv.player.level();
        // TODO: Add slots for inventory and machine
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, MachineRegistries.DARK_CHAMBER.getBlock());
    }
}