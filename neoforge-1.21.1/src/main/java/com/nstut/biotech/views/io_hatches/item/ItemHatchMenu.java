package com.nstut.biotech.views.io_hatches.item;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.ItemHatchBlockEntity;
import com.nstut.biotech.views.machines.menu.MachineMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class ItemHatchMenu extends MachineMenu {
    private final ItemHatchBlockEntity blockEntity;
    private final Level level;

    public ItemHatchMenu(MenuType<?> menu, int pContainerId, Inventory inventory, BlockEntity blockEntity) {
        super(menu, pContainerId);
        this.blockEntity = (ItemHatchBlockEntity) blockEntity;
        this.level = inventory.player.level();

        for (Slot slot : createHatchSlots(this.blockEntity.getInternalItemStorage())) {
            addSlot(slot);
        }
        addInventorySlots(inventory);
    }

    public static List<Slot> createHatchSlots(IItemHandler handler) {
        List<Slot> slots = new ArrayList<>(9);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                slots.add(new SlotItemHandler(handler, col + row * 3, 62 + col * 18, 17 + row * 18));
            }
        }
        return List.copyOf(slots);
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
