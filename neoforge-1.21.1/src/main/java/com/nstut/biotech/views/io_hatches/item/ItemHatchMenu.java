package com.nstut.biotech.views.io_hatches.item;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.ItemHatchBlockEntity;
import com.nstut.biotech.views.machines.menu.MachineMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ItemHatchMenu extends MachineMenu {
    private final ItemHatchBlockEntity blockEntity;
    private final Level level;

    public ItemHatchMenu(MenuType<?> menu, int pContainerId, Inventory inventory, BlockEntity blockEntity) {
        super(menu, pContainerId);
        this.blockEntity = (ItemHatchBlockEntity) blockEntity;
        this.level = inventory.player.level();

        var handler = this.blockEntity.getInternalItemStorage();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlot(new SlotItemHandler(handler, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }

        addInventorySlots(inventory);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return adaptiveQuickMoveStack(pIndex, 9, 9);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistries.ITEM_INPUT_HATCH.get())
                || stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistries.ITEM_OUTPUT_HATCH.get());
    }
}
